import { useCallback, useState } from 'react';
import { router, useFocusEffect } from 'expo-router';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Button, Card, Chip, Text } from 'react-native-paper';

import { useAuth } from '../../context/AuthContext';
import { listarCategorias, listarServicos } from '../../services/catalogoService';
import { colors } from '../../theme';

export default function ListaServicos() {
  const { usuario } = useAuth();
  const [categorias, setCategorias] = useState([]);
  const [categoriaId, setCategoriaId] = useState(null);
  const [servicos, setServicos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(false);
  const carregar = useCallback(async (estaAtivo = () => true) => {
      setCarregando(true);
      setErro(false);
      try {
        const [listaCategorias, listaServicos] = await Promise.all([listarCategorias(), listarServicos(categoriaId)]);
        if (estaAtivo()) { setCategorias(listaCategorias); setServicos(listaServicos); }
      } catch {
        if (estaAtivo()) setErro(true);
      } finally {
        if (estaAtivo()) setCarregando(false);
      }
  }, [categoriaId]);

  useFocusEffect(useCallback(() => {
    let ativo = true;
    carregar(() => ativo);
    return () => { ativo = false; };
  }, [carregar]));

  return (
    <ScrollView contentContainerStyle={styles.conteudo}>
      <Text variant="headlineSmall" style={styles.titulo}>Serviços da comunidade</Text>
      <Text variant="bodyMedium">Encontre serviços oferecidos por prestadores.</Text>
      {usuario?.tipo === 'PROVIDER' ? (
        <Button mode="contained" style={styles.acao} onPress={() => router.push('/servicos/formulario')}>Publicar serviço</Button>
      ) : null}
      <View style={styles.filtros}>
        <Chip selected={!categoriaId} onPress={() => setCategoriaId(null)}>Todas</Chip>
        {categorias.map((categoria) => (
          <Chip key={categoria.id} selected={categoriaId === categoria.id}
            onPress={() => setCategoriaId(categoria.id)}>{categoria.nome}</Chip>
        ))}
      </View>
      {carregando ? <ActivityIndicator style={styles.acao} /> : erro ? (
        <View>
          <Text>Não foi possível carregar os serviços. Verifique a conexão.</Text>
          <Button onPress={() => carregar()}>Tentar novamente</Button>
        </View>
      ) : servicos.length === 0 ? <Text>Nenhum serviço encontrado nesta categoria.</Text> : (
        servicos.map((servico) => (
          <Card key={servico.id} style={styles.cartao} onPress={() => router.push({ pathname: '/servicos/[id]', params: { id: servico.id } })}>
            {servico.imagemUrl ? <Card.Cover source={{ uri: servico.imagemUrl }} /> : null}
            <Card.Content>
              <Text variant="titleMedium">{servico.titulo}</Text>
              <Text>{servico.categoria.nome}</Text>
              <Text>R$ {Number(servico.valor).toFixed(2).replace('.', ',')} · {servico.duracaoMinutos} min</Text>
            </Card.Content>
          </Card>
        ))
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  conteudo: { flexGrow: 1, padding: 24, backgroundColor: colors.background },
  titulo: { color: colors.text, fontWeight: '700', marginBottom: 8 },
  acao: { marginTop: 18 },
  filtros: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginVertical: 20 },
  cartao: { marginBottom: 12, backgroundColor: colors.surface },
});
