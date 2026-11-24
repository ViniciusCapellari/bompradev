# Backend

O backend será composto por aplicações Spring Boot independentes. Cada microsserviço terá seu próprio ciclo de build, configuração, migrações e persistência.

## Estrutura inicial

- `api-gateway`: ponto de entrada das requisições externas e validação dos tokens.
- `auth-service`: contas, perfis, credenciais e emissão de tokens.
- `catalog-service`: serviços publicados pelos providers.
- `scheduling-service`: disponibilidade, solicitações e decisões de agendamento.

Os serviços serão adicionados gradualmente. A infraestrutura local inicial está descrita no arquivo `compose.yaml` da raiz.

## Versões da fundação

- Java 21
- Spring Boot 4.1.2
- Spring Cloud 2025.1.4
- PostgreSQL 17
- Consul 1.21

## Compilação

Cada projeto possui seu próprio Maven Wrapper. No Windows, por exemplo:

```powershell
.\auth-service\mvnw.cmd -f .\auth-service\pom.xml clean package
```

Para validar todo o ambiente declarado, execute na raiz do repositório:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose config
```

Antes da execução, substitua `POSTGRES_PASSWORD` e `JWT_SECRET` no arquivo `.env`. A chave JWT deve ser aleatória, ter pelo menos 32 bytes e não deve ser compartilhada nem versionada. É possível gerá-la com `openssl rand -base64 32` e inserir o resultado em `JWT_SECRET`. Se o banco já existir em um volume, sua senha continua sendo a definida na criação do volume: alterar `POSTGRES_PASSWORD` no `.env` não altera automaticamente a senha armazenada no PostgreSQL. O arquivo `.env` não deve ser versionado.

`AUTH_DB_PORT` altera somente a porta do PostgreSQL de autenticação no computador local. `AUTH_DATA_VOLUME` escolhe o volume Docker desse banco; o padrão mantém o volume existente. Não substitua ou apague um volume com dados sem primeiro preservar uma cópia e confirmar seu conteúdo.

## Configuração centralizada

O Compose inicia o Consul e, em seguida, o serviço `consul-config`, que executa `infra/consul/load-config.sh`. O script cadastra no armazenamento de chaves e valores as portas internas, URLs dos bancos e nomes de usuário iniciais dos serviços. Cada serviço aguarda o carregamento e busca suas configurações no Consul antes de iniciar. Isso adapta o padrão apresentado no material do Order API.

O script cria apenas chaves ausentes: ele não sobrescreve configurações já alteradas. Os valores são preservados no volume Docker `consul-data`. Os serviços também podem ser configurados por variáveis de ambiente quando executados fora do Compose. `POSTGRES_PASSWORD` e `JWT_SECRET` continuam no arquivo local `.env` e não são carregados no Consul.

Para consultar uma configuração não sensível no ambiente local:

```powershell
docker compose exec consul consul kv get config/catalog-service/server.port
```

Alterações em valores do Consul podem exigir a reinicialização do serviço correspondente. A mudança da porta interna também exige ajustar o mapeamento de portas no Compose. Não altere o usuário ou senha dos bancos existentes sem planejar a migração dos dados; mudar o arquivo `.env` ou o Consul não altera automaticamente as credenciais já gravadas no PostgreSQL.

O Consul está configurado como um único servidor para desenvolvimento, sem configuração de ACL ou TLS para produção. Sua interface e as portas dos bancos e serviços internos ficam vinculadas ao `localhost`; o gateway continua acessível pela rede local para permitir testes em dispositivo físico. Em redes compartilhadas, use apenas dados fictícios e mantenha o firewall ativo. Não exponha esse ambiente à internet nem armazene senhas ou chaves no Consul de desenvolvimento.

## Observabilidade local

O Compose inclui o Grafana com coleta local de logs, métricas e rastreamentos via OpenTelemetry. Com o ambiente iniciado, acesse `http://localhost:3000` (ou a porta `GRAFANA_PORT` configurada) e entre com o usuário e a senha padrão `admin`. Em **Explore**, selecione Loki para logs, Prometheus para métricas ou Tempo para rastreamentos. Uma consulta de horários pode ser acompanhada entre gateway, agenda e catálogo.

Essa configuração é destinada a desenvolvimento: a interface fica restrita ao computador local, usa credenciais padrão e não deve ser exposta à internet. Os dados da observabilidade são temporários e podem ser perdidos ao recriar o container. A primeira execução precisa baixar a imagem do Grafana.

Ao encerrar o ambiente, `docker compose down` preserva os volumes. Não use `docker compose down -v` sem antes confirmar e preservar os dados: esse comando também remove volumes do projeto.

## Cadastro inicial

Com o gateway, o `auth-service`, o Consul e o banco em execução, envie `POST /auth/signup` ao gateway na porta 8080:

```json
{
  "nome": "Ana Exemplo",
  "email": "ana@example.com",
  "senha": "senha123",
  "provider": true
}
```

O campo `provider` seleciona o perfil `PROVIDER`; ausente ou `false`, cria um `USER`. `ADMIN` não pode ser escolhido no cadastro público. A senha precisa ter pelo menos 8 caracteres e no máximo 72 bytes em UTF-8; ela é armazenada como hash BCrypt e não aparece na resposta. E-mails são únicos, independentemente de maiúsculas ou minúsculas.

## Login

Envie `POST /auth/signin` ao gateway na porta 8080 com o e-mail e a senha cadastrados:

```json
{
  "email": "ana@example.com",
  "senha": "senha123"
}
```

A resposta contém `token`, `tipoToken`, `expiraEmSegundos` e os dados de `usuario`. O token expira após uma hora. Credenciais incorretas, usuários inativos e tokens inválidos são recusados.

## Perfil e gateway

Os seguintes caminhos usam o gateway na porta 8080. Nas rotas protegidas, envie `Authorization: Bearer <token>`:

- `GET /auth/me`: consulta a própria conta autenticada.
- `PUT /auth/me`: atualiza o nome, a biografia e o portfólio da própria conta. Exemplo: `{"nome":"Ana","biografia":"Desenvolvedora","portfolio":"https://exemplo.com"}`.
- `POST /auth/me/provider`: permite a uma conta `USER` ativar o perfil `PROVIDER` e devolve um novo token.
- `GET /perfis/{id}`: consulta pública do nome, tipo, biografia e portfólio, sem retornar o e-mail.

O gateway consulta o Consul para localizar o auth-service e valida o JWT nas rotas protegidas. O auth-service também valida o token. As regras específicas de acesso a serviços e à moderação serão aplicadas junto com esses recursos.

Para iniciar apenas a infraestrutura necessária à etapa de acesso e perfis, execute na raiz:

```powershell
docker compose up -d --build observabilidade auth-service catalog-service api-gateway
```

A inicialização pode demorar enquanto as imagens são compiladas e os serviços registrados. A migração `V2` acrescenta os campos de perfil ao banco de autenticação existente sem substituir a migração anterior.

O login segue o exemplo `auth-api` do professor Luciano: usa `AuthenticationManager`, `UserDetailsService`, BCrypt e JJWT 0.13.0. As rotas e o filtro do gateway foram adaptados do exemplo `gateway-api` dele. A rota `/auth/me` foi inspirada no material do professor Augusto; no BompraDev, ela retorna diretamente os dados do usuário, sem o objeto `user` que envolve a resposta no exemplo dele. As regras de perfil e ativação de prestador são adaptações ao escopo do BompraDev. A chave é fornecida por variável de ambiente e não fica no código.

## Catálogo de serviços

O `catalog-service` mantém categorias e serviços em seu próprio banco. A migração `V2` cria as categorias iniciais de tecnologia. Serviços criados por `PROVIDER` ficam disponíveis imediatamente, usando o estado interno `APROVADO`; esse nome de estado não significa aprovação prévia por um administrador. Título, descrição, preço, duração em minutos e categoria são obrigatórios.

Pelo gateway na porta 8080:

- `GET /catalogo/categorias`: categorias ativas.
- `GET /catalogo/servicos`: lista pública; aceita filtro opcional `?categoriaId=<id>`.
- `GET /catalogo/servicos/{id}`: detalhes públicos.
- `GET /catalogo/meus-servicos`: serviços do provider autenticado.
- `POST /catalogo/servicos`: publica um serviço do provider autenticado.
- `PUT /catalogo/servicos/{id}`: altera um serviço disponível pertencente ao provider autenticado.
- `DELETE /catalogo/servicos/{id}`: inativa o serviço do provider, sem remover os agendamentos existentes.
- `POST /catalogo/servicos/{id}/imagem`: envia uma imagem JPEG ou PNG de até 5 MB no campo `imagem` de um formulário multipart.

Nas rotas protegidas, envie `Authorization: Bearer <token>`. Exemplo de corpo para cadastro e edição:

```json
{"categoriaId":"<id-da-categoria>","titulo":"Serviço","descricao":"Descrição do serviço","valor":120.00,"duracaoMinutos":60}
```

O gateway e o catálogo validam o JWT, e o catálogo verifica o perfil e a propriedade do serviço. Serviços inativos deixam de aparecer no catálogo e não aceitam novas solicitações; os agendamentos já registrados permanecem. A moderação administrativa ainda não está implementada. A organização de entidades, repositórios, serviços e controllers adapta os exemplos do `product-api` do professor Luciano; as regras de publicação e propriedade pertencem ao BompraDev.

O envio de imagens usa Cloudinary, como no exemplo do professor Augusto, com uma adaptação: o envio é realizado pelo backend depois de verificar o usuário e o arquivo. Para habilitá-lo, crie uma conta e coloque a URL de credenciais `CLOUDINARY_URL` no arquivo `.env` local, sem enviá-la ao GitHub ou ao aplicativo. Após configurar, recrie o serviço com `docker compose up -d --build catalog-service`. Sem essa configuração, o restante do catálogo funciona, mas o envio de imagens fica indisponível. Ao trocar uma imagem, o endereço antigo deixa de ser usado no aplicativo, mas o arquivo anterior continua na conta Cloudinary até ser removido manualmente.

## Agenda

O prestador cadastra períodos semanais e responde às solicitações recebidas. O cliente consulta os horários pela duração do serviço, solicita um horário e acompanha o estado em seu perfil. Os horários seguem o fuso de Brasília.

A consulta da agenda ao catálogo usa limite de espera, novas tentativas para falhas temporárias e circuit breaker. A criação do agendamento não é repetida automaticamente. Quando uma solicitação é recusada, o período volta a aparecer como disponível, desde que ainda não tenha passado e continue dentro da disponibilidade do prestador.

Novas solicitações guardam o preço informado pelo catálogo no momento da solicitação. Depois da aprovação pelo prestador, o cliente pode registrar um pagamento simulado, sem movimentação financeira. A aprovação não registra o pagamento automaticamente. Registros anteriores ao preço histórico não possuem valor e não permitem esse pagamento. Cancelamento e remarcação ainda não foram implementados.

Pelo gateway, com token Bearer:

- `GET` e `POST /agenda/disponibilidades` e `DELETE /agenda/disponibilidades/{id}`: manutenção dos horários do prestador.
- `GET /agenda/horarios?servicoId={id}&data=AAAA-MM-DD`: horários disponíveis.
- `POST /agenda/agendamentos`: solicita um horário com `servicoId`, `data` e `horario`.
- `GET /agenda/agendamentos/meus`: solicitações do cliente.
- `GET /agenda/agendamentos/recebidos`: solicitações dos serviços do prestador.
- `PUT /agenda/agendamentos/{id}/aprovar` e `PUT /agenda/agendamentos/{id}/recusar`: resposta pelo prestador responsável.
- `PUT /agenda/agendamentos/{id}/pagamento`: registro de pagamento simulado pelo cliente responsável, após aprovação.

