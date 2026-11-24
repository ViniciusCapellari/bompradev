export function validarAcesso(email, senha) {
  const erros = {};
  if (!email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) erros.email = 'Informe um e-mail válido.';
  if (!senha.trim()) erros.senha = 'Informe sua senha.';
  return erros;
}

export function validarCadastro(nome, email, senha) {
  const erros = validarAcesso(email, senha);
  if (!nome.trim()) erros.nome = 'Informe seu nome.';
  if (senha.length < 8) erros.senha = 'A senha deve ter pelo menos 8 caracteres.';
  return erros;
}

export function mensagemErro(erro, alternativa) {
  if (erro?.status === 401) return 'E-mail ou senha inválidos, ou sessão expirada.';
  if (erro?.status === 409) return 'Este e-mail já está cadastrado.';
  if (erro?.status === 400) return 'Confira os dados informados.';
  return alternativa;
}
