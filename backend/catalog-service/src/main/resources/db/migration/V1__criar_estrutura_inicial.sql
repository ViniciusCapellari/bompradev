CREATE TABLE categorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(80) NOT NULL,
    descricao VARCHAR(255),
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_categorias_nome ON categorias (LOWER(nome));

CREATE TABLE servicos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID NOT NULL,
    categoria_id UUID NOT NULL,
    titulo VARCHAR(120) NOT NULL,
    descricao TEXT NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    duracao_minutos INTEGER NOT NULL,
    imagem_url VARCHAR(500),
    situacao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_servicos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id),
    CONSTRAINT ck_servicos_valor CHECK (valor >= 0),
    CONSTRAINT ck_servicos_duracao CHECK (duracao_minutos > 0),
    CONSTRAINT ck_servicos_situacao CHECK (
        situacao IN ('PENDENTE', 'APROVADO', 'REJEITADO', 'INATIVO')
    )
);

CREATE INDEX idx_servicos_provider ON servicos (provider_id);
CREATE INDEX idx_servicos_categoria ON servicos (categoria_id);
CREATE INDEX idx_servicos_situacao ON servicos (situacao);
