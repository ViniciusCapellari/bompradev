import { useCallback, useState } from 'react';
import { Redirect, useFocusEffect } from 'expo-router';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Button, Card, HelperText, Text } from 'react-native-paper';
import { useAuth } from '../context/AuthContext';
import { listarAgendamentosRecebidos, responderAgendamento } from '../services/agendaService';
import { consultarPerfilPublico } from '../services/authService';
import { consultarServico } from '../services/catalogoService';
import { colors } from '../theme';

const situacoes = { PENDENTE: 'Aguardando resposta', APROVADO: 'Aprovado', RECUSADO: 'Recusado', CANCELADO: 'Cancelado', CONCLUIDO: 'Concluído' };
const formatarData = valor => new Intl.DateTimeFormat('pt-BR', { timeZone: 'America/Sao_Paulo', day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(valor));

export default function Solicitacoes() {
  const { usuario, token, inicializando } = useAuth();
  const [solicitacoes, setSolicitacoes] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [respondendo, setRespondendo] = useState(null);
  const [mensagem, setMensagem] = useState('');
  const [erro, setErro] = useState(false);

  useFocusEffect(useCallback(() => {
    let ativo = true;
    async function carregar() {
      try {
        const lista = await listarAgendamentosRecebidos(token);
        const itens = await Promise.all(lista.map(async item => {
          const [servico, cliente] = await Promise.allSettled([consultarServico(item.servicoId), consultarPerfilPublico(item.userId)]);
          return { ...item, titulo: servico.status === 'fulfilled' ? servico.value.titulo : 'Serviço indisponível',
            cliente: cliente.status === 'fulfilled' ? cliente.value.nome : 'Cliente' };
        }));
        if (ativo) { setSolicitacoes(itens); setErro(false); setMensagem(''); }
      } catch { if (ativo) { setErro(true); setMensagem('Não foi possível carregar as solicitações.'); } }
      finally { if (ativo) setCarregando(false); }
    }
    if (token && usuario?.tipo === 'PROVIDER') carregar();
    return () => { ativo = false; };
  }, [token, usuario?.tipo]));

  if (inicializando) return <ActivityIndicator style={styles.carregamento} />;
  if (!usuario) return <Redirect href="/login" />;
  if (usuario.tipo !== 'PROVIDER') return <Redirect href="/perfil" />;

  async function responder(id, aprovar) {
    setRespondendo(id); setMensagem('');
    try {
      const resultado = await responderAgendamento(token, id, aprovar);
      setSolicitacoes(atuais => atuais.map(item => item.id === id ? { ...item, situacao: resultado.situacao } : item));
      setErro(false); setMensagem(aprovar ? 'Solicitação aprovada.' : 'Solicitação recusada. O horário voltou a ficar disponível.');
    } catch (falha) {
      setErro(true);
      setMensagem(falha.status === 409 ? 'A solicitação já foi respondida. Atualize a tela.' : 'Não foi possível responder à solicitação.');
    } finally { setRespondendo(null); }
  }

  return (
    <ScrollView contentContainerStyle={styles.conteudo}>
      <Text variant="titleLarge" style={styles.titulo}>Solicitações recebidas</Text>
      <Text style={styles.texto}>Horários de Brasília.</Text>
      {mensagem ? <HelperText type={erro ? 'error' : 'info'}>{mensagem}</HelperText> : null}
      {carregando ? <ActivityIndicator /> : solicitacoes.length === 0 ? <Text>Nenhuma solicitação recebida.</Text> : solicitacoes.map(item => (
        <Card key={item.id} style={styles.cartao}><Card.Content>
          <Text variant="titleMedium">{item.titulo}</Text>
          <Text>Cliente: {item.cliente}</Text>
          <Text>{formatarData(item.inicio)}</Text>
          <Text>{item.valorContratado == null ? 'Valor não registrado' : `Valor: R$ ${Number(item.valorContratado).toFixed(2).replace('.', ',')}`}</Text>
          <Text>{situacoes[item.situacao] ?? item.situacao}</Text>
          {item.situacaoPagamento === 'PAGO' ? <Text>Pagamento simulado realizado</Text>
            : item.situacao === 'APROVADO' ? <Text>Pagamento simulado não realizado</Text> : null}
          {item.situacao === 'PENDENTE' ? <View style={styles.acoes}>
            <Button mode="contained" onPress={() => responder(item.id, true)} disabled={respondendo !== null}>Aprovar</Button>
            <Button mode="outlined" onPress={() => responder(item.id, false)} disabled={respondendo !== null}>Recusar</Button>
          </View> : null}
        </Card.Content></Card>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  carregamento: { flex: 1 },
  conteudo: { flexGrow: 1, padding: 24, backgroundColor: colors.background },
  titulo: { color: colors.text, fontWeight: '700', marginBottom: 8 },
  texto: { marginBottom: 20 },
  cartao: { backgroundColor: colors.surface, marginBottom: 12 },
  acoes: { flexDirection: 'row', gap: 8, marginTop: 12 },
});
