import { useState } from 'react';
import { router } from 'expo-router';
import { Button, HelperText } from 'react-native-paper';
import CampoTexto from '../components/CampoTexto';
import TelaAcesso from '../components/TelaAcesso';
import { useAuth } from '../context/AuthContext';
import { mensagemErro, validarAcesso } from '../utils/validacao';

export default function LoginScreen() {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erros, setErros] = useState({});
  const [mensagem, setMensagem] = useState('');
  const [carregando, setCarregando] = useState(false);
  const { entrar } = useAuth();

  async function acessar() {
    const validacao = validarAcesso(email, senha);
    setErros(validacao);
    if (Object.keys(validacao).length) return;
    setMensagem(''); setCarregando(true);
    try { await entrar(email, senha); router.replace('/perfil'); }
    catch (erro) { setMensagem(mensagemErro(erro, 'Não foi possível acessar. Verifique sua conexão e tente novamente.')); }
    finally { setCarregando(false); }
  }

  return (
    <TelaAcesso titulo="Boas-vindas!" subtitulo="Entre para acessar sua conta.">
      <CampoTexto label="E-mail" valor={email} setValor={setEmail} erro={erros.email} autoCapitalize="none" keyboardType="email-address" autoComplete="email" />
      <CampoTexto label="Senha" valor={senha} setValor={setSenha} erro={erros.senha} secureTextEntry autoComplete="password" />
      {mensagem ? <HelperText type="error">{mensagem}</HelperText> : null}
      <Button mode="contained" onPress={acessar} loading={carregando} disabled={carregando} contentStyle={{ paddingVertical: 7 }}>Entrar</Button>
      <Button mode="text" onPress={() => router.push('/cadastro')} disabled={carregando}>Ainda não tem conta? Cadastre-se</Button>
    </TelaAcesso>
  );
}
