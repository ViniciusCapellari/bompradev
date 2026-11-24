# Aplicativo mobile

Aplicativo do BompraDev desenvolvido com React Native, Expo SDK 58 e JavaScript. É possível testar acesso e perfil, consultar serviços e, como provider, publicar ou editar os próprios serviços. As seções BOMPRADROP, BOMPRALAB, BOMPRAVOZ e BOMPRAPRÊMIO exibem conteúdo local de demonstração, sem integração ao backend. A enquete não registra votos e os prêmios mostrados não correspondem a sorteios ou resgates ativos. Agendamentos ainda não foram implementados.

## Requisitos

- Node.js 24
- npm
- Expo Go ou emulador Android compatível

## Configuração

Copie o arquivo de exemplo e informe o endereço do API Gateway:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
```

O endereço padrão é `http://localhost:8080`, adequado ao navegador na mesma máquina. No emulador Android, normalmente use `http://10.0.2.2:8080`. Em um dispositivo físico, coloque em `EXPO_PUBLIC_API_URL` o IP local da máquina que executa o backend, por exemplo `http://192.168.1.10:8080`. O celular e o computador precisam estar na mesma rede e a porta 8080 deve estar acessível. Após modificar o arquivo, reinicie o Expo.

## Execução

```powershell
npm.cmd install
npm.cmd run start
```

## Estrutura

- `src/app`: rotas e layouts do Expo Router.
- `src/components`: componentes reutilizáveis.
- `src/data`: conteúdos demonstrativos locais das seções.
- `src/services`: comunicação com serviços externos.
- `src/storage`: adapters de armazenamento local seguro.
- `src/theme`: cores, espaçamentos e tipografia.

A imagem `assets/marca-original.jpg` contém a arte de referência fornecida para o projeto. Os logotipos são apresentados nas telas a partir de partes dessa imagem, sem alterar o arquivo original.

Inicie antes o backend conforme o respectivo README, incluindo o `catalog-service`. Com o Expo em execução, utilize o QR Code no Expo Go ou abra o emulador. Para uma visualização no navegador do computador, execute `npm.cmd run web`. A sessão é armazenada no SecureStore em dispositivos nativos e na sessão do navegador na versão web. As telas de acesso, campos reutilizáveis e estrutura de listagem e detalhes foram adaptados dos exemplos React Native do professor Augusto.

