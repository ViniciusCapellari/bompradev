package br.edu.atitus.bompradev.scheduling.dtos;

import br.edu.atitus.bompradev.scheduling.entidades.Disponibilidade;
import java.time.LocalTime;
import java.util.UUID;

public record DisponibilidadeResponse(UUID id, Short diaSemana, LocalTime horaInicio, LocalTime horaFim) {
    public static DisponibilidadeResponse de(Disponibilidade disponibilidade) {
        return new DisponibilidadeResponse(disponibilidade.getId(), disponibilidade.getDiaSemana(),
                disponibilidade.getHoraInicio(), disponibilidade.getHoraFim());
    }
}
