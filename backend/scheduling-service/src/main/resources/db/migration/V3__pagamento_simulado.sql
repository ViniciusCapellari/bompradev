ALTER TABLE agendamentos ADD COLUMN situacao_pagamento VARCHAR(20) NOT NULL DEFAULT 'NAO_REALIZADO';

ALTER TABLE agendamentos ADD CONSTRAINT ck_agendamentos_situacao_pagamento
    CHECK (situacao_pagamento IN ('NAO_REALIZADO', 'PAGO'));
