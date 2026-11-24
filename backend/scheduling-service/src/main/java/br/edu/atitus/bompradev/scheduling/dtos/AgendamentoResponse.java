package br.edu.atitus.bompradev.scheduling.dtos;

import br.edu.atitus.bompradev.scheduling.entidades.Agendamento;
import br.edu.atitus.bompradev.scheduling.entidades.SituacaoAgendamento;
import br.edu.atitus.bompradev.scheduling.entidades.SituacaoPagamento;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

public record AgendamentoResponse(UUID id, UUID servicoId, UUID userId, UUID providerId, Instant inicio, Instant fim, BigDecimal valorContratado,
                                  SituacaoAgendamento situacao, SituacaoPagamento situacaoPagamento) {
    public static AgendamentoResponse de(Agendamento agendamento) {
        return new AgendamentoResponse(agendamento.getId(), agendamento.getServicoId(), agendamento.getUserId(), agendamento.getProviderId(),
                agendamento.getInicio(), agendamento.getFim(), agendamento.getValorContratado(), agendamento.getSituacao(),
                agendamento.getSituacaoPagamento());
    }
}
