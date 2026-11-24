package br.edu.atitus.bompradev.auth.controllers;

import br.edu.atitus.bompradev.auth.dtos.LoginResponse;
import br.edu.atitus.bompradev.auth.dtos.PerfilPublicoResponse;
import br.edu.atitus.bompradev.auth.dtos.PerfilRequest;
import br.edu.atitus.bompradev.auth.dtos.UsuarioResponse;
import br.edu.atitus.bompradev.auth.servicos.PerfilService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
public class PerfilController {
    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/perfis/{id}")
    public PerfilPublicoResponse consultar(@PathVariable UUID id) {
        return perfilService.consultarPublico(id);
    }

    @PutMapping("/auth/me")
    public UsuarioResponse atualizar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilRequest dados) {
        return perfilService.atualizar(UUID.fromString(jwt.getSubject()), dados);
    }

    @PostMapping("/auth/me/provider")
    public LoginResponse tornarProvider(@AuthenticationPrincipal Jwt jwt) {
        return perfilService.tornarProvider(UUID.fromString(jwt.getSubject()));
    }
}
