import { StyleSheet, Text, View } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import MarcaBompra from './MarcaBompra';

import { colors, spacing, typography } from '../theme';

export function CartaoCategoria({ titulo, descricao, icone, marca }) {
  return (
    <View style={styles.card}>
      {marca ? <MarcaBompra variante={marca} tamanho={56} />
        : icone ? <View style={styles.icone}><MaterialCommunityIcons name={icone} size={25} color={colors.primary} /></View> : null}
      <View style={styles.informacoes}>
        <Text style={styles.title}>{titulo}</Text>
        <Text style={styles.description}>{descricao}</Text>
      </View>
      <MaterialCommunityIcons name="chevron-right" size={24} color={colors.textMuted} />
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    padding: spacing.md,
    borderColor: colors.border,
    borderRadius: 12,
    borderWidth: 1,
    backgroundColor: colors.surface,
  },
  icone: {
    width: 48,
    height: 48,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#F8E4CF',
  },
  informacoes: { flex: 1, gap: spacing.xs },
  title: {
    ...typography.cardTitle,
    color: colors.primary,
  },
  description: {
    ...typography.body,
    color: colors.textMuted,
  },
});

