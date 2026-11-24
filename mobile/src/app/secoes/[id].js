import { useState } from 'react';
import { Stack, useLocalSearchParams } from 'expo-router';
import { ScrollView, StyleSheet, View } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Button, RadioButton, Text } from 'react-native-paper';

import MarcaBompra from '../../components/MarcaBompra';
import { buscarSecao } from '../../data/secoes';
import { colors, spacing } from '../../theme';

export default function SecaoScreen() {
  const { id } = useLocalSearchParams();
  const secao = typeof id === 'string' ? buscarSecao(id) : null;
  const [opcao, setOpcao] = useState('');
  const [respondida, setRespondida] = useState(false);

  if (!secao) {
    return <Text style={styles.conteudo}>Seção não encontrada.</Text>;
  }

  return (
    <>
      <Stack.Screen options={{ title: secao.nome }} />
      <ScrollView contentContainerStyle={styles.conteudo}>
        <View style={styles.cabecalho}>
          <View style={styles.emblema}><MarcaBompra variante={secao.id} tamanho={76} /></View>
          <Text variant="headlineSmall" style={styles.titulo}>{secao.titulo}</Text>
          <Text variant="bodyLarge" style={styles.introducao}>{secao.introducao}</Text>
        </View>

        <View style={styles.aviso}>
          <MaterialCommunityIcons name="information-outline" size={19} color={colors.primary} />
          <Text style={styles.avisoTexto}>
            {secao.id === 'voz'
              ? 'Enquete demonstrativa: a escolha não é registrada.'
              : secao.id === 'premio'
                ? 'Exemplos ilustrativos. Não há sorteios ou resgates ativos nesta tela.'
                : 'Conteúdo de exemplo para apresentar a seção.'}
          </Text>
        </View>

        {secao.id === 'voz' ? (
          <View style={styles.enquete}>
            <Text variant="titleLarge" style={styles.tituloCartao}>{secao.pergunta}</Text>
            <RadioButton.Group onValueChange={setOpcao} value={opcao}>
              {secao.opcoes.map((item) => (
                <RadioButton.Item key={item} label={item} value={item} disabled={respondida}
                  position="leading" labelStyle={styles.opcao} />
              ))}
            </RadioButton.Group>
            {respondida ? (
              <>
                <Text style={styles.mensagem}>Escolha de demonstração: {opcao}. Nada foi salvo.</Text>
                <Button mode="outlined" onPress={() => { setOpcao(''); setRespondida(false); }}>Refazer escolha</Button>
              </>
            ) : (
              <Button mode="contained" disabled={!opcao} onPress={() => setRespondida(true)}>Confirmar escolha</Button>
            )}
          </View>
        ) : secao.itens.map((item) => (
          <View key={item.titulo} style={styles.cartao}>
            <View style={styles.iconeCartao}>
              <MaterialCommunityIcons name={item.icone} size={24} color={colors.primary} />
            </View>
            <View style={styles.textoCartao}>
              <Text variant="titleMedium" style={styles.tituloCartao}>{item.titulo}</Text>
              <Text variant="bodyMedium" style={styles.descricao}>{item.texto}</Text>
            </View>
          </View>
        ))}
      </ScrollView>
    </>
  );
}

const styles = StyleSheet.create({
  conteudo: { flexGrow: 1, padding: spacing.lg, paddingBottom: 40, backgroundColor: colors.background },
  cabecalho: { padding: 22, borderRadius: 18, backgroundColor: colors.primary, marginBottom: 20 },
  emblema: { marginBottom: 16 },
  titulo: { color: colors.onPrimary, fontWeight: '700' },
  introducao: { color: colors.primaryMuted, marginTop: 8, lineHeight: 23 },
  aviso: { flexDirection: 'row', alignItems: 'flex-start', gap: 9, padding: 13, borderRadius: 12,
    backgroundColor: '#F8E4CF', marginBottom: 20 },
  avisoTexto: { flex: 1, color: colors.text, lineHeight: 20 },
  cartao: { flexDirection: 'row', gap: 14, padding: 18, backgroundColor: colors.surface,
    borderColor: colors.border, borderWidth: 1, borderRadius: 15, marginBottom: 12 },
  iconeCartao: { width: 42, height: 42, borderRadius: 12, alignItems: 'center', justifyContent: 'center',
    backgroundColor: '#F8E4CF' },
  textoCartao: { flex: 1 },
  tituloCartao: { color: colors.text, fontWeight: '700', marginBottom: 8 },
  descricao: { color: colors.textMuted, lineHeight: 21 },
  enquete: { padding: 18, backgroundColor: colors.surface, borderRadius: 15,
    borderWidth: 1, borderColor: colors.border },
  opcao: { textAlign: 'left' },
  mensagem: { marginVertical: 14, color: colors.textMuted },
});
