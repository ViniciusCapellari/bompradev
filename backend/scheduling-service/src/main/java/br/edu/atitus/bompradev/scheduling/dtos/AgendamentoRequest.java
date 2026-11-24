package br.edu.atitus.bompradev.scheduling.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AgendamentoRequest(@NotNull UUID servicoId, @NotNull LocalDate data, @NotNull LocalTime horario) {}
