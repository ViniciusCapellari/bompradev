package br.edu.atitus.bompradev.scheduling.dtos;

import java.util.UUID;
import java.math.BigDecimal;

public record ServicoCatalogo(UUID id, UUID providerId, Integer duracaoMinutos, BigDecimal valor) {}
