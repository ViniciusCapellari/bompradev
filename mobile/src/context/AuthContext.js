import { createContext, useCallback, useContext, useEffect, useState } from 'react';

import { ativarProvider, atualizarConta, cadastrarConta, consultarConta, entrarConta } from '../services/authService';
import { obterSessao, removerSessao, salvarSessao } from '../storage/sessionStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [token, setToken] = useState(null);
  const [inicializando, setInicializando] = useState(true);

  const sair = useCallback(async () => {
    try { await removerSessao(); } finally { setUsuario(null); setToken(null); }
  }, []);

  useEffect(() => {
    let ativo = true;
    async function restaurarSessao() {
      try {
        const salvo = await obterSessao();
        if (!salvo) return;
        const conta = await consultarConta(salvo);
        if (ativo) { setToken(salvo); setUsuario(conta); }
      } catch (erro) {
        if (erro.status === 401) await removerSessao();
      } finally {
        if (ativo) setInicializando(false);
      }
    }
    restaurarSessao();
    return () => { ativo = false; };
  }, []);

  async function guardarSessao(sessao) {
    await salvarSessao(sessao.token);
    setToken(sessao.token);
    setUsuario(sessao.usuario);
  }

  async function entrar(email, senha) {
    await guardarSessao(await entrarConta(email, senha));
  }

  async function cadastrar(dados) {
    await cadastrarConta(dados);
    try { await entrar(dados.email, dados.senha); }
    catch {
      const falha = new Error('Conta criada. Entre com seu e-mail e senha para continuar.');
      falha.contaCriada = true;
      throw falha;
    }
  }

  async function atualizarPerfil(dados) {
    try { const conta = await atualizarConta(token, dados); setUsuario(conta); }
    catch (erro) { if (erro.status === 401) await sair(); throw erro; }
  }

  async function tornarProvider() {
    try { await guardarSessao(await ativarProvider(token)); }
    catch (erro) { if (erro.status === 401) await sair(); throw erro; }
  }

  return <AuthContext.Provider value={{ usuario, token, inicializando, entrar, cadastrar, sair, atualizarPerfil, tornarProvider }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
