package br.edu.atitus.bompradev.auth.dtos;

import br.edu.atitus.bompradev.auth.entidades.TipoUsuario;
import br.edu.atitus.bompradev.auth.entidades.Usuario;

import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, TipoUsuario tipo, String biografia, String portfolio) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipo(), usuario.getBiografia(), usuario.getPortfolio());
    }
}
