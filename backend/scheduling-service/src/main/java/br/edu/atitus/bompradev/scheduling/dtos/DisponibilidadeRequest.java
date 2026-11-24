package br.edu.atitus.bompradev.scheduling.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record DisponibilidadeRequest(@NotNull @Min(0) @Max(6) Short diaSemana,
                                    @NotNull LocalTime horaInicio,
                                    @NotNull LocalTime horaFim) {}
