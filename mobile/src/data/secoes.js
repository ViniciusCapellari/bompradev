export const secoes = [
  {
    id: 'drop',
    nome: 'BOMPRA DROP',
    titulo: 'BOMPRADROP',
    descricao: 'Dicas rápidas para aplicar no dia a dia.',
    icone: 'lightning-bolt',
    introducao: 'Ideias curtas para facilitar a rotina de quem trabalha com tecnologia.',
    itens: [
      {
        titulo: 'Divida um problema em partes',
        texto: 'Antes de começar a programar, anote o resultado esperado e separe a tarefa em passos menores. Isso ajuda a identificar dúvidas e acompanhar o progresso.',
        icone: 'format-list-checks',
      },
      {
        titulo: 'Dê nomes que expliquem a intenção',
        texto: 'Use nomes claros para funções e variáveis. Um nome que descreve o propósito facilita a leitura quando você ou seu colega voltar ao código.',
        icone: 'code-tags',
      },
      {
        titulo: 'Teste uma mudança por vez',
        texto: 'Depois de alterar uma tela ou regra, experimente os cenários principais e também uma entrada inválida. Assim fica mais fácil identificar a origem de um problema.',
        icone: 'check-circle-outline',
      },
    ],
  },
  {
    id: 'lab',
    nome: 'BOMPRA LAB',
    titulo: 'BOMPRALAB',
    descricao: 'Guias e conteúdos para aprofundar conhecimentos.',
    icone: 'flask-outline',
    introducao: 'Materiais introdutórios para explorar conceitos e praticar.',
    itens: [
      {
        titulo: 'Organizando uma tela mobile',
        texto: 'Separe elementos reutilizáveis, como campos e cartões, em componentes. Mantenha a tela responsável pela organização e pelo comportamento daquela parte do aplicativo.',
        icone: 'view-dashboard-outline',
      },
      {
        titulo: 'Como uma API participa do aplicativo',
        texto: 'A tela envia uma requisição para o backend, aguarda a resposta e apresenta os dados. Durante a espera ou diante de uma falha, é importante informar o usuário.',
        icone: 'connection',
      },
      {
        titulo: 'Primeiros passos com testes',
        texto: 'Escolha uma regra, descreva o resultado esperado e teste uma situação válida e outra inválida. Repita os testes depois de alterações relacionadas.',
        icone: 'test-tube',
      },
    ],
  },
  {
    id: 'voz',
    nome: 'BOMPRA VOZ',
    titulo: 'BOMPRAVOZ',
    descricao: 'Enquetes e pesquisas para a comunidade.',
    icone: 'poll',
    introducao: 'Um espaço para conhecer os interesses da comunidade.',
    pergunta: 'Qual assunto você gostaria de explorar primeiro?',
    opcoes: ['Desenvolvimento web', 'Aplicativos mobile', 'Boas práticas de código', 'Infraestrutura e redes'],
  },
  {
    id: 'premio',
    nome: 'BOMPRA PRÊMIO',
    titulo: 'BOMPRAPRÊMIO',
    descricao: 'Experiências promocionais para a comunidade.',
    icone: 'gift-outline',
    introducao: 'Conheça exemplos do que poderá aparecer nesta seção.',
    itens: [
      {
        titulo: 'Kit de estudos',
        texto: 'Exemplo de um conjunto de materiais para acompanhar a sua rotina de aprendizado.',
        icone: 'book-open-page-variant-outline',
      },
      {
        titulo: 'Acessórios para a mesa',
        texto: 'Exemplo de uma seleção de acessórios para deixar o espaço de trabalho mais organizado.',
        icone: 'desk',
      },
    ],
  },
];

export function buscarSecao(id) {
  return secoes.find((secao) => secao.id === id);
}
