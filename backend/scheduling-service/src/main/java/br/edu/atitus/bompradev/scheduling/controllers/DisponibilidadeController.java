package br.edu.atitus.bompradev.scheduling.controllers;

import br.edu.atitus.bompradev.scheduling.dtos.DisponibilidadeRequest;
import br.edu.atitus.bompradev.scheduling.dtos.DisponibilidadeResponse;
import br.edu.atitus.bompradev.scheduling.servicos.DisponibilidadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agenda/disponibilidades")
public class DisponibilidadeController {
    private final DisponibilidadeService disponibilidades;

    public DisponibilidadeController(DisponibilidadeService disponibilidades) { this.disponibilidades = disponibilidades; }

    @GetMapping
    public List<DisponibilidadeResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return disponibilidades.listar(UUID.fromString(jwt.getSubject()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibilidadeResponse cadastrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DisponibilidadeRequest dados) {
        return disponibilidades.cadastrar(UUID.fromString(jwt.getSubject()), dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        disponibilidades.remover(UUID.fromString(jwt.getSubject()), id);
    }
}
