import { useEffect, useState } from 'react';
import { Redirect } from 'expo-router';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Button, HelperText, SegmentedButtons, Text } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import CampoTexto from '../components/CampoTexto';
import { useAuth } from '../context/AuthContext';
import { cadastrarDisponibilidade, listarDisponibilidades, removerDisponibilidade } from '../services/agendaService';
import { colors } from '../theme';

const dias = ['Domingo', 'Segunda-feira', 'Terça-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sábado'];
const abreviacoes = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const horarioValido = valor => /^([01]\d|2[0-3]):[0-5]\d$/.test(valor);

export default function DisponibilidadeScreen() {
  const { usuario, token, inicializando } = useAuth();
  const [dia, setDia] = useState('1');
  const [inicio, setInicio] = useState('');
  const [fim, setFim] = useState('');
  const [periodos, setPeriodos] = useState([]);
  const [carregando, setCarregando] = useState(false);
  const [mensagem, setMensagem] = useState('');
  const [erro, setErro] = useState(false);

  useEffect(() => {
    let ativo = true;
    if (token && usuario?.tipo === 'PROVIDER') {
      listarDisponibilidades(token).then(dados => { if (ativo) setPeriodos(dados); })
        .catch(() => { if (ativo) { setErro(true); setMensagem('Não foi possível carregar seus horários.'); } });
    }
    return () => { ativo = false; };
  }, [token, usuario?.tipo]);

  if (inicializando) return <ActivityIndicator style={styles.carregamento} />;
  if (!usuario) return <Redirect href="/login" />;
  if (usuario.tipo !== 'PROVIDER') return <Redirect href="/perfil" />;

  async function salvar() {
    if (!horarioValido(inicio) || !horarioValido(fim) || inicio >= fim) {
      setErro(true); setMensagem('Informe horários válidos no formato HH:MM, com início antes do fim.'); return;
    }
    setCarregando(true); setMensagem('');
    try {
      const novo = await cadastrarDisponibilidade(token, { diaSemana: Number(dia), horaInicio: inicio, horaFim: fim });
      setPeriodos(atuais => [...atuais, novo].sort((a, b) => a.diaSemana - b.diaSemana || a.horaInicio.localeCompare(b.horaInicio)));
      setInicio(''); setFim(''); setErro(false); setMensagem('Horário semanal cadastrado.');
    } catch (falha) {
      setErro(true);
      setMensagem(falha.status === 409 ? 'Já existe um horário cadastrado nesse período.' : 'Não foi possível cadastrar o horário.');
    } finally { setCarregando(false); }
  }

  async function remover(id) {
    setCarregando(true); setMensagem('');
    try {
      await removerDisponibilidade(token, id);
      setPeriodos(atuais => atuais.filter(periodo => periodo.id !== id));
      setErro(false); setMensagem('Horário removido.');
    } catch { setErro(true); setMensagem('Não foi possível remover o horário.'); }
    finally { setCarregando(false); }
  }

  return (
    <SafeAreaView style={styles.safeArea} edges={['bottom']}>
      <ScrollView contentContainerStyle={styles.conteudo} keyboardShouldPersistTaps="handled">
        <Text variant="titleLarge" style={styles.titulo}>Horários semanais</Text>
        <Text style={styles.texto}>Informe os períodos em que você costuma atender. Eles se repetem toda semana.</Text>
        <Text style={styles.rotulo}>Dia da semana</Text>
        <SegmentedButtons value={dia} onValueChange={setDia} buttons={abreviacoes.slice(0, 4).map((label, indice) => ({ label, value: String(indice) }))} />
        <View style={styles.linhaDias}>
          <SegmentedButtons value={dia} onValueChange={setDia} buttons={abreviacoes.slice(4).map((label, indice) => ({ label, value: String(indice + 4) }))} />
        </View>
        <CampoTexto label="Início (HH:MM)" valor={inicio} setValor={setInicio} maxLength={5} placeholder="09:00" />
        <CampoTexto label="Fim (HH:MM)" valor={fim} setValor={setFim} maxLength={5} placeholder="17:00" />
        {mensagem ? <HelperText type={erro ? 'error' : 'info'}>{mensagem}</HelperText> : null}
        <Button mode="contained" onPress={salvar} disabled={carregando} loading={carregando}>Adicionar horário</Button>
        <Text variant="titleMedium" style={styles.listaTitulo}>Meus horários</Text>
        {periodos.length === 0 ? <Text>Nenhum horário cadastrado.</Text> : periodos.map(periodo => (
          <View key={periodo.id} style={styles.periodo}>
            <View><Text variant="titleSmall">{dias[periodo.diaSemana]}</Text><Text>{periodo.horaInicio.slice(0, 5)} às {periodo.horaFim.slice(0, 5)}</Text></View>
            <Button onPress={() => remover(periodo.id)} disabled={carregando}>Remover</Button>
          </View>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  carregamento: { flex: 1 },
  conteudo: { padding: 24, paddingBottom: 40 },
  titulo: { color: colors.text, fontWeight: '700', marginBottom: 8 },
  texto: { color: colors.text, marginBottom: 16 },
  rotulo: { marginBottom: 8 },
  linhaDias: { marginTop: 8, marginBottom: 16 },
  listaTitulo: { marginTop: 28, marginBottom: 12, fontWeight: '700' },
  periodo: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 8 },
});
