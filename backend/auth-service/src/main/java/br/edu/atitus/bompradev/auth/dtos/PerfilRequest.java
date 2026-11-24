package br.edu.atitus.bompradev.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilRequest(@NotBlank @Size(max = 120) String nome,
                            @Size(max = 1000) String biografia,
                            @Size(max = 500) String portfolio) {
}
