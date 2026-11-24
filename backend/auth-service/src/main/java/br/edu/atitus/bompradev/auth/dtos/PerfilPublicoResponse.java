package br.edu.atitus.bompradev.auth.dtos;

import br.edu.atitus.bompradev.auth.entidades.TipoUsuario;
import br.edu.atitus.bompradev.auth.entidades.Usuario;
import java.util.UUID;

public record PerfilPublicoResponse(UUID id, String nome, TipoUsuario tipo, String biografia, String portfolio) {
    public static PerfilPublicoResponse de(Usuario usuario) {
        return new PerfilPublicoResponse(usuario.getId(), usuario.getNome(), usuario.getTipo(), usuario.getBiografia(), usuario.getPortfolio());
    }
}
