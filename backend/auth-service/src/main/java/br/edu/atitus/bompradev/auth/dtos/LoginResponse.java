package br.edu.atitus.bompradev.auth.dtos;

public record LoginResponse(String token, String tipoToken, long expiraEmSegundos, UsuarioResponse usuario) {
}
