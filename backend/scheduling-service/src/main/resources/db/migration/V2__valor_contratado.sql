ALTER TABLE agendamentos ADD COLUMN valor_contratado NUMERIC(10, 2);

ALTER TABLE agendamentos ADD CONSTRAINT ck_agendamentos_valor_contratado
    CHECK (valor_contratado IS NULL OR valor_contratado >= 0);
