import { useState } from 'react';
import { Redirect, router } from 'expo-router';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Button, HelperText, Text } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import CampoTexto from '../components/CampoTexto';
import { useAuth } from '../context/AuthContext';
import { colors } from '../theme';
import { mensagemErro } from '../utils/validacao';

export default function PerfilScreen() {
  const { usuario, inicializando } = useAuth();
  if (inicializando) return <ActivityIndicator style={styles.carregamento} />;
  if (!usuario) return <Redirect href="/login" />;
  return <FormularioPerfil key={usuario.id} usuario={usuario} />;
}

function FormularioPerfil({ usuario }) {
  const { sair, atualizarPerfil, tornarProvider } = useAuth();
  const [nome, setNome] = useState(usuario.nome ?? '');
  const [biografia, setBiografia] = useState(usuario.biografia ?? '');
  const [portfolio, setPortfolio] = useState(usuario.portfolio ?? '');
  const [mensagem, setMensagem] = useState('');
  const [erro, setErro] = useState(false);
  const [carregando, setCarregando] = useState(false);

  async function salvar() {
    if (!nome.trim()) { setErro(true); setMensagem('Informe seu nome.'); return; }
    setCarregando(true); setMensagem('');
    try {
      await atualizarPerfil({ nome: nome.trim(), biografia, portfolio });
      setErro(false); setMensagem('Perfil atualizado.');
    } catch (falha) { setErro(true); setMensagem(mensagemErro(falha, 'Não foi possível salvar o perfil.')); }
    finally { setCarregando(false); }
  }

  async function ativar() {
    setCarregando(true); setMensagem('');
    try { await tornarProvider(); setErro(false); setMensagem('Seu perfil de prestador está ativo.'); }
    catch (falha) { setErro(true); setMensagem(mensagemErro(falha, 'Não foi possível ativar o perfil de prestador.')); }
    finally { setCarregando(false); }
  }

  async function encerrar() {
    setCarregando(true);
    try { await sair(); router.replace('/'); }
    catch { setErro(true); setMensagem('Não foi possível encerrar a sessão.'); }
    finally { setCarregando(false); }
  }

  return (
    <SafeAreaView style={styles.safeArea} edges={['bottom']}>
      <ScrollView contentContainerStyle={styles.conteudo} keyboardShouldPersistTaps="handled">
        <View style={styles.cabecalho}>
          <Text variant="headlineSmall" style={styles.titulo}>{usuario.nome}</Text>
          <Text style={styles.detalhe}>{usuario.email}</Text>
          <Text style={styles.detalhe}>{usuario.tipo === 'PROVIDER' ? 'Prestador de serviços' : usuario.tipo === 'ADMIN' ? 'Administrador' : 'Usuário'}</Text>
        </View>
        <Text variant="titleLarge" style={styles.secao}>Editar perfil</Text>
        <CampoTexto label="Nome" valor={nome} setValor={setNome} autoCapitalize="words" maxLength={120} />
        <CampoTexto label="Biografia" valor={biografia} setValor={setBiografia} multiline numberOfLines={4} maxLength={1000} />
        <CampoTexto label="Portfólio ou link" valor={portfolio} setValor={setPortfolio} multiline maxLength={500} />
        {mensagem ? <HelperText type={erro ? 'error' : 'info'}>{mensagem}</HelperText> : null}
        <Button mode="contained" onPress={salvar} disabled={carregando} loading={carregando}>Salvar alterações</Button>
        <Button mode="outlined" style={styles.botao} onPress={() => router.push({ pathname: '/perfil/[id]', params: { id: usuario.id } })} disabled={carregando}>Ver perfil público</Button>
        {usuario.tipo !== 'ADMIN' ? <Button mode="outlined" style={styles.botao} onPress={() => router.push('/meus-agendamentos')} disabled={carregando}>Meus agendamentos</Button> : null}
        {usuario.tipo === 'PROVIDER' ? <Button mode="outlined" style={styles.botao} onPress={() => router.push('/disponibilidade')} disabled={carregando}>Minha disponibilidade</Button> : null}
        {usuario.tipo === 'PROVIDER' ? <Button mode="outlined" style={styles.botao} onPress={() => router.push('/solicitacoes')} disabled={carregando}>Solicitações recebidas</Button> : null}
        {usuario.tipo === 'USER' ? <Button mode="outlined" style={styles.botao} onPress={ativar} disabled={carregando}>Oferecer serviços como prestador</Button> : null}
        <Button mode="text" style={styles.botao} onPress={encerrar} disabled={carregando}>Sair da conta</Button>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  carregamento: { flex: 1 },
  conteudo: { padding: 24, paddingBottom: 40 },
  cabecalho: { padding: 20, borderRadius: 16, backgroundColor: colors.primary, marginBottom: 24 },
  titulo: { color: colors.onPrimary, fontWeight: '700' },
  detalhe: { color: colors.primaryMuted, marginTop: 6 },
  secao: { color: colors.text, marginBottom: 18, fontWeight: '700' },
  botao: { marginTop: 12 },
});
