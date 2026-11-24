import { ScrollView, StyleSheet, View } from 'react-native';
import { Text } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors } from '../theme';

export default function TelaAcesso({ titulo, subtitulo, children }) {
  return (
    <SafeAreaView style={styles.safeArea} edges={['bottom']}>
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <View style={styles.cabecalho}>
          <Text variant="headlineMedium" style={styles.marca}>BompraDev</Text>
          <Text variant="bodyLarge" style={styles.chamada}>Comunidade e serviços de tecnologia</Text>
        </View>
        <View style={styles.formulario}>
          <Text variant="headlineSmall" style={styles.titulo}>{titulo}</Text>
          <Text variant="bodyMedium" style={styles.subtitulo}>{subtitulo}</Text>
          {children}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.surface },
  scroll: { flexGrow: 1, paddingBottom: 32 },
  cabecalho: { alignItems: 'center', backgroundColor: colors.primary, borderBottomLeftRadius: 38, borderBottomRightRadius: 38, paddingVertical: 38, paddingHorizontal: 24 },
  marca: { color: colors.onPrimary, fontWeight: '800' },
  chamada: { color: colors.primaryMuted, marginTop: 8, textAlign: 'center' },
  formulario: { marginHorizontal: 28, marginTop: 32 },
  titulo: { fontWeight: '800', color: colors.text },
  subtitulo: { color: colors.textMuted, marginBottom: 24, marginTop: 6 },
});
