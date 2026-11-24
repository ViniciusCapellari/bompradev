import { useEffect, useState } from 'react';
import { Redirect, router, useLocalSearchParams } from 'expo-router';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Button, Chip, HelperText, Text } from 'react-native-paper';

import CampoTexto from '../../components/CampoTexto';
import { useAuth } from '../../context/AuthContext';
import { consultarServico, listarCategorias, salvarServico } from '../../services/catalogoService';
import { colors } from '../../theme';

export default function FormularioServico() {
  const { id } = useLocalSearchParams();
  const { usuario, token, inicializando } = useAuth();
  const [categorias, setCategorias] = useState([]);
  const [categoriaId, setCategoriaId] = useState('');
  const [titulo, setTitulo] = useState('');
  const [descricao, setDescricao] = useState('');
  const [valor, setValor] = useState('');
  const [duracao, setDuracao] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');
  const [falhaInicial, setFalhaInicial] = useState(false);

  useEffect(() => {
    let ativo = true;
    async function carregar() {
      try {
        const [lista, servico] = await Promise.all([listarCategorias(), id ? consultarServico(id) : Promise.resolve(null)]);
        if (!ativo) return;
        setCategorias(lista);
        if (servico) {
          if (servico.providerId !== usuario?.id) { setFalhaInicial(true); return; }
          setCategoriaId(servico.categoria.id);
          setTitulo(servico.titulo);
          setDescricao(servico.descricao);
          setValor(String(servico.valor).replace('.', ','));
          setDuracao(String(servico.duracaoMinutos));
        }
      } catch {
        if (ativo) setFalhaInicial(true);
      } finally {
        if (ativo) setCarregando(false);
      }
    }
    if (usuario?.tipo === 'PROVIDER') carregar();
    return () => { ativo = false; };
  }, [id, usuario?.id, usuario?.tipo]);

  async function salvar() {
    const textoValor = valor.trim().replace(',', '.');
    if (!categoriaId || !titulo.trim() || !descricao.trim() || !/^(\d{1,8})(\.\d{1,2})?$/.test(textoValor)
        || !/^\d+$/.test(duracao) || Number(duracao) < 1 || Number(duracao) > 2147483647) {
      setErro('Informe categoria, título, descrição, preço válido e duração em minutos.');
      return;
    }
    setSalvando(true);
    setErro('');
    try {
      await salvarServico(token, { categoriaId, titulo: titulo.trim(), descricao: descricao.trim(),
        valor: Number(textoValor), duracaoMinutos: Number(duracao) }, id);
      router.replace('/servicos');
    } catch (falha) {
      setErro(falha.status === 401 ? 'Sua sessão expirou. Entre novamente.'
        : falha.status === 403 ? 'Você não tem permissão para alterar este serviço.'
          : 'Não foi possível salvar. Confira os dados e a conexão.');
    } finally { setSalvando(false); }
  }

  if (inicializando) return <ActivityIndicator style={{ flex: 1 }} />;
  if (!usuario) return <Redirect href="/login" />;
  if (usuario.tipo !== 'PROVIDER') return <Redirect href="/servicos" />;
  if (carregando) return <ActivityIndicator style={{ flex: 1 }} />;
  if (falhaInicial) return <Text style={styles.conteudo}>Não foi possível carregar os dados para edição.</Text>;

  return (
    <ScrollView contentContainerStyle={styles.conteudo} keyboardShouldPersistTaps="handled">
      <Text variant="headlineSmall" style={styles.titulo}>{id ? 'Editar serviço' : 'Publicar serviço'}</Text>
      <Text variant="titleMedium" style={styles.rotulo}>Categoria</Text>
      <View style={styles.categorias}>
        {categorias.map((categoria) => (
          <Chip key={categoria.id} selected={categoriaId === categoria.id}
            onPress={() => setCategoriaId(categoria.id)}>{categoria.nome}</Chip>
        ))}
      </View>
      <CampoTexto label="Título" valor={titulo} setValor={setTitulo} maxLength={120} />
      <CampoTexto label="Descrição" valor={descricao} setValor={setDescricao} multiline numberOfLines={4} />
      <CampoTexto label="Preço (R$)" valor={valor} setValor={setValor} keyboardType="decimal-pad" />
      <CampoTexto label="Duração em minutos" valor={duracao} setValor={setDuracao} keyboardType="number-pad" />
      {erro ? <HelperText type="error">{erro}</HelperText> : null}
      <Button mode="contained" onPress={salvar} loading={salvando} disabled={salvando}>
        {id ? 'Salvar alterações' : 'Publicar'}
      </Button>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  conteudo: { flexGrow: 1, padding: 24, backgroundColor: colors.background },
  titulo: { color: colors.text, fontWeight: '700', marginBottom: 20 },
  rotulo: { marginBottom: 10 },
  categorias: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 20 },
});
