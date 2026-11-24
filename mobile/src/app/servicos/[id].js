import { useEffect, useState } from 'react';
import { router, useLocalSearchParams } from 'expo-router';
import { Image, Platform, ScrollView, StyleSheet } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { ActivityIndicator, Button, Dialog, Portal, Text } from 'react-native-paper';
import { useAuth } from '../../context/AuthContext';
import { consultarServico, enviarImagemServico, inativarServico } from '../../services/catalogoService';
import { colors } from '../../theme';

export default function DetalheServico() {
  const { id } = useLocalSearchParams();
  const { usuario, token } = useAuth();
  const [servico, setServico] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [confirmando, setConfirmando] = useState(false);
  const [inativando, setInativando] = useState(false);
  const [erro, setErro] = useState('');
  const [erroImagem, setErroImagem] = useState('');
  const [enviandoImagem, setEnviandoImagem] = useState(false);

  async function escolherImagem() {
    setErroImagem('');
    try {
      const resultado = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], quality: 1 });
      if (resultado.canceled) return;
      const arquivo = resultado.assets[0];
      if (arquivo.fileSize > 5 * 1024 * 1024 || !['image/jpeg', 'image/png'].includes(arquivo.mimeType)) {
        setErroImagem('Escolha uma imagem JPEG ou PNG de até 5 MB.');
        return;
      }
      setEnviandoImagem(true);
      const imagem = Platform.OS === 'web' ? arquivo.file : {
        uri: arquivo.uri, name: arquivo.fileName || 'imagem.jpg', type: arquivo.mimeType,
      };
      if (!imagem) throw new Error('Arquivo indisponível');
      setServico(await enviarImagemServico(token, id, imagem));
    } catch (falha) {
      setErroImagem(falha.status === 503 ? 'O envio de imagens ainda não foi configurado.'
        : 'Não foi possível enviar a imagem. Verifique o arquivo e tente novamente.');
    } finally {
      setEnviandoImagem(false);
    }
  }

  async function confirmarInativacao() {
    setInativando(true);
    setErro('');
    try {
      await inativarServico(token, id);
      setConfirmando(false);
      router.replace('/servicos');
    } catch {
      setErro('Não foi possível inativar o serviço. Verifique sua sessão e tente novamente.');
    } finally {
      setInativando(false);
    }
  }

  useEffect(() => {
    let ativo = true;
    async function carregar() {
      try { const resultado = await consultarServico(id); if (ativo) setServico(resultado); }
      catch { if (ativo) setServico(null); }
      finally { if (ativo) setCarregando(false); }
    }
    if (typeof id === 'string') carregar();
    return () => { ativo = false; };
  }, [id]);

  if (carregando) return <ActivityIndicator style={{ flex: 1 }} />;
  if (!servico) return <Text style={styles.conteudo}>Não foi possível carregar este serviço.</Text>;
  return (
    <ScrollView contentContainerStyle={styles.conteudo}>
      {servico.imagemUrl ? <Image source={{ uri: servico.imagemUrl }} style={styles.imagem} /> : null}
      <Text variant="headlineSmall" style={styles.titulo}>{servico.titulo}</Text>
      <Text>{servico.categoria.nome}</Text>
      <Text style={styles.secao}>R$ {Number(servico.valor).toFixed(2).replace('.', ',')} · {servico.duracaoMinutos} min</Text>
      <Text variant="titleMedium" style={styles.secao}>Descrição</Text>
      <Text variant="bodyLarge">{servico.descricao}</Text>
      <Button mode="outlined" style={styles.secao}
        onPress={() => router.push({ pathname: '/perfil/[id]', params: { id: servico.providerId } })}>Ver perfil do prestador</Button>
      {usuario?.tipo === 'PROVIDER' && usuario.id === servico.providerId ? (
        <>
          <Button mode="contained" style={styles.secao}
            onPress={() => router.push({ pathname: '/servicos/formulario', params: { id: servico.id } })}>Editar serviço</Button>
          <Button mode="outlined" style={styles.secao} onPress={escolherImagem}
            loading={enviandoImagem} disabled={enviandoImagem}>Adicionar ou trocar imagem</Button>
          {erroImagem ? <Text style={styles.erro}>{erroImagem}</Text> : null}
          <Button mode="outlined" style={styles.secao} onPress={() => setConfirmando(true)}>Inativar serviço</Button>
        </>
      ) : null}
      {usuario?.tipo !== 'ADMIN' && usuario?.id !== servico.providerId ? (
        <Button mode="contained" style={styles.secao}
          onPress={() => usuario ? router.push({ pathname: '/agendamento', params: { servicoId: servico.id, titulo: servico.titulo } }) : router.push('/login')}>
          {usuario ? 'Consultar horários' : 'Entre para consultar horários'}
        </Button>
      ) : null}
      <Portal>
        <Dialog visible={confirmando} onDismiss={() => !inativando && setConfirmando(false)}>
          <Dialog.Title>Inativar serviço?</Dialog.Title>
          <Dialog.Content>
            <Text>Novas solicitações serão bloqueadas. Os agendamentos existentes serão mantidos.</Text>
            {erro ? <Text style={styles.erro}>{erro}</Text> : null}
          </Dialog.Content>
          <Dialog.Actions>
            <Button disabled={inativando} onPress={() => setConfirmando(false)}>Voltar</Button>
            <Button loading={inativando} disabled={inativando} onPress={confirmarInativacao}>Inativar</Button>
          </Dialog.Actions>
        </Dialog>
      </Portal>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  conteudo: { flexGrow: 1, padding: 24, backgroundColor: colors.background },
  titulo: { color: colors.text, fontWeight: '700', marginBottom: 8 },
  secao: { marginTop: 20 },
  erro: { color: '#b3261e', marginTop: 12 },
  imagem: { width: '100%', height: 200, borderRadius: 8, marginBottom: 20 },
});
