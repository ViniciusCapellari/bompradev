import { requisicaoApi } from './apiClient';

export function listarCategorias() {
  return requisicaoApi('/catalogo/categorias');
}

export function listarServicos(categoriaId) {
  const filtro = categoriaId ? `?categoriaId=${encodeURIComponent(categoriaId)}` : '';
  return requisicaoApi(`/catalogo/servicos${filtro}`);
}

export function consultarServico(id) {
  return requisicaoApi(`/catalogo/servicos/${encodeURIComponent(id)}`);
}

export function salvarServico(token, dados, id) {
  const caminho = id ? `/catalogo/servicos/${encodeURIComponent(id)}` : '/catalogo/servicos';
  return requisicaoApi(caminho, {
    method: id ? 'PUT' : 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: JSON.stringify(dados),
  });
}

export function inativarServico(token, id) {
  return requisicaoApi(`/catalogo/servicos/${encodeURIComponent(id)}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` },
  });
}

export function enviarImagemServico(token, id, imagem) {
  const dados = new FormData();
  dados.append('imagem', imagem);
  return requisicaoApi(`/catalogo/servicos/${encodeURIComponent(id)}/imagem`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: dados,
  });
}
