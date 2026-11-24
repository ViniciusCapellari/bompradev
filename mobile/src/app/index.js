import { router } from 'expo-router';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { CartaoCategoria } from '../components/CartaoCategoria';
import MarcaBompra from '../components/MarcaBompra';
import { useAuth } from '../context/AuthContext';
import { secoes } from '../data/secoes';
import { colors, spacing, typography } from '../theme';

const categorias = [
  ...secoes.map((secao) => ({ ...secao, caminho: { pathname: '/secoes/[id]', params: { id: secao.id } } })),
  { id: 'servicos', titulo: 'SERVIÇOS', descricao: 'Serviços de tecnologia oferecidos por providers.',
    icone: 'briefcase-outline', caminho: '/servicos' },
];

export default function HomeScreen() {
  const { usuario } = useAuth();
  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.header}>
          <View style={styles.brandRow}>
            <MarcaBompra tamanho={64} />
            <Text style={styles.brand}>BompraDev</Text>
          </View>
          <Text style={styles.subtitle}>
            Conteúdo, comunidade e serviços para quem vive tecnologia.
          </Text>
          <Pressable
            accessibilityRole="button"
            accessibilityLabel={usuario ? 'Meu perfil' : 'Acessar conta'}
            onPress={() => router.push(usuario ? '/perfil' : '/login')}
            style={({ pressed }) => [styles.accessButton, pressed && styles.pressed]}
          >
            <Text style={styles.accessButtonText}>{usuario ? 'Meu perfil' : 'Acessar conta'}</Text>
          </Pressable>
        </View>

        <View style={styles.categories}>
          <Text style={styles.sectionTitle}>Explore a plataforma</Text>
          {categorias.map((categoria) => (
            <Pressable key={categoria.id} accessibilityRole="button" accessibilityLabel={`Abrir ${categoria.titulo}`}
              onPress={() => router.push(categoria.caminho)}
              style={({ pressed }) => pressed && styles.pressed}>
              <CartaoCategoria titulo={categoria.titulo} descricao={categoria.descricao} icone={categoria.icone}
                marca={categoria.id !== 'servicos' ? categoria.id : null} />
            </Pressable>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.primary,
  },
  content: {
    flexGrow: 1,
    backgroundColor: colors.background,
  },
  header: {
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xl,
    paddingTop: spacing.lg,
    backgroundColor: colors.primary,
  },
  brand: {
    ...typography.title,
    color: colors.onPrimary,
  },
  brandRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
  },
  subtitle: {
    ...typography.body,
    color: colors.primaryMuted,
  },
  accessButton: {
    minHeight: 48,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: spacing.sm,
    borderRadius: 10,
    backgroundColor: colors.accent,
  },
  accessButtonText: {
    ...typography.button,
    color: colors.onAccent,
  },
  pressed: {
    opacity: 0.8,
  },
  categories: {
    gap: spacing.md,
    padding: spacing.lg,
  },
  sectionTitle: {
    ...typography.heading,
    color: colors.text,
  },
});

