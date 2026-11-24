import { useState } from 'react';
import { Redirect, router, useLocalSearchParams } from 'expo-router';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Button, Chip, HelperText, Text } from 'react-native-paper';
import CampoTexto from '../components/CampoTexto';
import { useAuth } from '../context/AuthContext';
import { listarHorarios, solicitarAgendamento } from '../services/agendaService';
import { colors } from '../theme';

function dataIso(valor) {
  const partes = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(valor);
  if (!partes) return null;
  const [, dia, mes, ano] = partes;
  const data = new Date(Date.UTC(Number(ano), Number(mes) - 1, Number(dia)));
  if (data.getUTCFullYear() !== Number(ano) || data.getUTCMonth() !== Number(mes) - 1 || data.getUTCDate() !== Number(dia)) return null;
  return `${ano}-${mes}-${dia}`;
}

export default function AgendamentoScreen() {
  const { servicoId, titulo } = useLocalSearchParams();
  const { usuario, token, inicializando } = useAuth();
  const [data, setData] = useState('');
  const [dataConsultada, setDataConsultada] = useState(null);
  const [horarios, setHorarios] = useState([]);
  const [horario, setHorario] = useState(null);
  const [carregando, setCarregando] = useState(false);
  const [mensagem, setMensagem] = useState('');
  const [erro, setErro] = useState(false);
  const [enviado, setEnviado] = useState(false);

  if (inicializando) return <ActivityIndicator style={styles.carregamento} />;
  if (!usuario) return <Redirect href="/login" />;
  if (usuario.tipo === 'ADMIN' || typeof servicoId !== 'string') return <Redirect href="/servicos" />;

  async function consultar() {
    const escolhida = dataIso(data);
    if (!escolhida) { setErro(true); setMensagem('Informe uma data válida no formato DD/MM/AAAA.'); return; }
    setCarregando(true); setMensagem(''); setHorario(null); setDataConsultada(null);
    try {
      const resultado = await listarHorarios(token, servicoId, escolhida);
      setHorarios(resultado); setDataConsultada(escolhida); setErro(false);
      if (!resultado.length) setMensagem('Não há horários disponíveis para esta data.');
    } catch { setErro(true); setMensagem('Não foi possível consultar os horários.'); }
    finally { setCarregando(false); }
  }

  async function solicitar() {
    if (!horario || !dataConsultada) return;
    setCarregando(true); setMensagem('');
    try {
      await solicitarAgendamento(token, { servicoId, data: dataConsultada, horario });
      setEnviado(true); setErro(false); setMensagem('Solicitação enviada. Aguarde a aprovação do prestador.');
    } catch (falha) {
      setErro(true); setHorario(null); setDataConsultada(null);
      setMensagem(falha.status === 409 ? 'Este horário não está mais disponível. Consulte novamente.' : 'Não foi possível solicitar o agendamento.');
    } finally { setCarregando(false); }
  }

  return (
    <ScrollView contentContainerStyle={styles.conteudo} keyboardShouldPersistTaps="handled">
      <Text variant="titleLarge" style={styles.titulo}>{titulo || 'Serviço'}</Text>
      <Text style={styles.texto}>Os horários seguem o fuso de Brasília. Sua solicitação precisa da aprovação do prestador.</Text>
      {!enviado ? <>
        <CampoTexto label="Data (DD/MM/AAAA)" valor={data} setValor={valor => { setData(valor); setDataConsultada(null); setHorario(null); }} placeholder="DD/MM/AAAA" maxLength={10} />
        <Button mode="outlined" onPress={consultar} disabled={carregando}>Consultar horários</Button>
        {dataConsultada && horarios.length ? <>
          <Text variant="titleMedium" style={styles.secao}>Escolha um horário</Text>
          <View style={styles.horarios}>{horarios.map(item => <Chip key={item} selected={horario === item} onPress={() => setHorario(item)}>{item.slice(0, 5)}</Chip>)}</View>
          <Button mode="contained" onPress={solicitar} disabled={!horario || carregando} loading={carregando}>Solicitar agendamento</Button>
        </> : null}
      </> : <Button mode="contained" onPress={() => router.replace('/meus-agendamentos')}>Ver meus agendamentos</Button>}
      {mensagem ? <HelperText type={erro ? 'error' : 'info'}>{mensagem}</HelperText> : null}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  carregamento: { flex: 1 },
  conteudo: { flexGrow: 1, padding: 24, backgroundColor: colors.background },
  titulo: { color: colors.text, fontWeight: '700', marginBottom: 12 },
  texto: { color: colors.text, marginBottom: 20 },
  secao: { marginTop: 24, marginBottom: 12 },
  horarios: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 20 },
});
