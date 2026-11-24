import { useEffect, useState } from 'react';
import { useLocalSearchParams } from 'expo-router';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Text } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { consultarPerfilPublico } from '../../services/authService';
import { colors } from '../../theme';

export default function PerfilPublicoScreen() {
  const { id } = useLocalSearchParams();
  const [perfil, setPerfil] = useState(null);
  const [falhou, setFalhou] = useState(false);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    let ativo = true;
    async function carregar() {
      try { const resultado = await consultarPerfilPublico(id); if (ativo) setPerfil(resultado); }
      catch { if (ativo) setFalhou(true); }
      finally { if (ativo) setCarregando(false); }
    }
    if (typeof id === 'string') carregar();
    return () => { ativo = false; };
  }, [id]);

  if (carregando) return <ActivityIndicator style={styles.carregamento} />;
  if (falhou || !perfil) return <Text style={styles.conteudo}>Não foi possível carregar este perfil.</Text>;

  return (
    <SafeAreaView style={styles.safeArea} edges={['bottom']}>
      <ScrollView contentContainerStyle={styles.conteudo}>
        <View style={styles.cabecalho}>
          <Text variant="headlineSmall" style={styles.titulo}>{perfil.nome}</Text>
          <Text style={styles.detalhe}>{perfil.tipo === 'PROVIDER' ? 'Prestador de serviços' : perfil.tipo === 'ADMIN' ? 'Administrador' : 'Usuário'}</Text>
        </View>
        <Text variant="titleLarge" style={styles.secao}>Sobre</Text>
        <Text variant="bodyLarge">{perfil.biografia || 'Nenhuma biografia informada.'}</Text>
        <Text variant="titleLarge" style={styles.secao}>Portfólio</Text>
        <Text variant="bodyLarge" selectable>{perfil.portfolio || 'Nenhum portfólio informado.'}</Text>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  carregamento: { flex: 1 },
  conteudo: { padding: 24, paddingBottom: 40 },
  cabecalho: { padding: 20, borderRadius: 16, backgroundColor: colors.primary },
  titulo: { color: colors.onPrimary, fontWeight: '700' },
  detalhe: { color: colors.primaryMuted, marginTop: 6 },
  secao: { color: colors.text, marginTop: 24, marginBottom: 8, fontWeight: '700' },
});
