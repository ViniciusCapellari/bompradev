import { useState } from 'react';
import { router } from 'expo-router';
import { Button, Checkbox, HelperText } from 'react-native-paper';
import CampoTexto from '../components/CampoTexto';
import TelaAcesso from '../components/TelaAcesso';
import { useAuth } from '../context/AuthContext';
import { mensagemErro, validarCadastro } from '../utils/validacao';

export default function CadastroScreen() {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [provider, setProvider] = useState(false);
  const [erros, setErros] = useState({});
  const [mensagem, setMensagem] = useState('');
  const [contaCriada, setContaCriada] = useState(false);
  const [carregando, setCarregando] = useState(false);
  const { cadastrar } = useAuth();

  async function criarConta() {
    const validacao = validarCadastro(nome, email, senha);
    setErros(validacao);
    if (Object.keys(validacao).length) return;
    setMensagem(''); setCarregando(true);
    try { await cadastrar({ nome: nome.trim(), email: email.trim(), senha, provider }); router.replace('/perfil'); }
    catch (erro) {
      setContaCriada(Boolean(erro.contaCriada));
      setMensagem(erro.contaCriada ? erro.message : mensagemErro(erro, 'Não foi possível cadastrar. Verifique sua conexão e tente novamente.'));
    } finally { setCarregando(false); }
  }

  return (
    <TelaAcesso titulo="Crie sua conta" subtitulo="Faça parte da comunidade BompraDev.">
      <CampoTexto label="Nome completo" valor={nome} setValor={setNome} erro={erros.nome} autoCapitalize="words" maxLength={120} />
      <CampoTexto label="E-mail" valor={email} setValor={setEmail} erro={erros.email} autoCapitalize="none" keyboardType="email-address" autoComplete="email" maxLength={180} />
      <CampoTexto label="Senha" valor={senha} setValor={setSenha} erro={erros.senha} secureTextEntry autoComplete="new-password" maxLength={72} />
      <Checkbox.Item label="Declaro que ofereço serviços como prestador" status={provider ? 'checked' : 'unchecked'} onPress={() => setProvider(!provider)} position="leading" />
      {mensagem ? <HelperText type={contaCriada ? 'info' : 'error'}>{mensagem}</HelperText> : null}
      <Button mode="contained" onPress={criarConta} loading={carregando} disabled={carregando || contaCriada} contentStyle={{ paddingVertical: 7 }}>Criar conta</Button>
      <Button mode="text" onPress={() => router.replace('/login')} disabled={carregando}>Já tenho conta</Button>
    </TelaAcesso>
  );
}
