import { requisicaoApi } from './apiClient';

export function cadastrarConta(dados) {
  return requisicaoApi('/auth/signup', { method: 'POST', body: JSON.stringify(dados) });
}

export function entrarConta(email, senha) {
  return requisicaoApi('/auth/signin', { method: 'POST', body: JSON.stringify({ email: email.trim(), senha }) });
}

export function consultarConta(token) {
  return requisicaoApi('/auth/me', { headers: { Authorization: `Bearer ${token}` } });
}

export function atualizarConta(token, dados) {
  return requisicaoApi('/auth/me', { method: 'PUT', headers: { Authorization: `Bearer ${token}` }, body: JSON.stringify(dados) });
}

export function ativarProvider(token) {
  return requisicaoApi('/auth/me/provider', { method: 'POST', headers: { Authorization: `Bearer ${token}` } });
}

export function consultarPerfilPublico(id) {
  return requisicaoApi(`/perfis/${encodeURIComponent(id)}`);
}
