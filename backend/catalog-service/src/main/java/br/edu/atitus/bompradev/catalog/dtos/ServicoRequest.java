package br.edu.atitus.bompradev.catalog.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicoRequest(
        @NotNull UUID categoriaId,
        @NotBlank @Size(max = 120) String titulo,
        @NotBlank String descricao,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal valor,
        @NotNull @Min(1) Integer duracaoMinutos) {
}
