const DEFAULT_API_URL = 'http://localhost:8080';

function obterUrlApi() {
  return process.env.EXPO_PUBLIC_API_URL ?? DEFAULT_API_URL;
}

export async function requisicaoApi(path, options = {}) {
  const response = await fetch(`${obterUrlApi()}${path}`, {
    ...options,
    headers: {
      Accept: 'application/json',
      ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
      ...options.headers,
    },
  });

  if (!response.ok) {
    const erro = new Error(`Falha na requisição: HTTP ${response.status}`);
    erro.status = response.status;
    throw erro;
  }

  if (response.status === 204) {
    return undefined;
  }

  return response.json();
}

