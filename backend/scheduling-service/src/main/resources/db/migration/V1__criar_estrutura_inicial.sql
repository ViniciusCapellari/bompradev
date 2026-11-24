CREATE TABLE disponibilidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID NOT NULL,
    dia_semana SMALLINT NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_disponibilidades_dia_semana CHECK (dia_semana BETWEEN 0 AND 6),
    CONSTRAINT ck_disponibilidades_horario CHECK (hora_inicio < hora_fim),
    CONSTRAINT uk_disponibilidades_horario UNIQUE (
        provider_id,
        dia_semana,
        hora_inicio,
        hora_fim
    )
);

CREATE INDEX idx_disponibilidades_provider ON disponibilidades (provider_id);

CREATE TABLE agendamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    servico_id UUID NOT NULL,
    user_id UUID NOT NULL,
    provider_id UUID NOT NULL,
    inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    fim TIMESTAMP WITH TIME ZONE NOT NULL,
    situacao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    observacao VARCHAR(500),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_agendamentos_periodo CHECK (inicio < fim),
    CONSTRAINT ck_agendamentos_situacao CHECK (
        situacao IN ('PENDENTE', 'APROVADO', 'RECUSADO', 'CANCELADO', 'CONCLUIDO')
    )
);

CREATE INDEX idx_agendamentos_user ON agendamentos (user_id);
CREATE INDEX idx_agendamentos_provider ON agendamentos (provider_id);
CREATE INDEX idx_agendamentos_servico ON agendamentos (servico_id);
CREATE INDEX idx_agendamentos_inicio ON agendamentos (inicio);
