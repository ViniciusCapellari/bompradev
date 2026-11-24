package br.edu.atitus.bompradev.scheduling.controllers;

import br.edu.atitus.bompradev.scheduling.dtos.AgendamentoRequest;
import br.edu.atitus.bompradev.scheduling.dtos.AgendamentoResponse;
import br.edu.atitus.bompradev.scheduling.servicos.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agenda")
public class AgendamentoController {
    private final AgendamentoService agenda;
    public AgendamentoController(AgendamentoService agenda) { this.agenda = agenda; }

    @GetMapping("/horarios")
    public List<LocalTime> horarios(@RequestParam UUID servicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return agenda.horarios(servicoId, data);
    }

    @PostMapping("/agendamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse solicitar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AgendamentoRequest dados) {
        return agenda.solicitar(UUID.fromString(jwt.getSubject()), dados);
    }

    @GetMapping("/agendamentos/meus")
    public List<AgendamentoResponse> meus(@AuthenticationPrincipal Jwt jwt) {
        return agenda.meus(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping("/agendamentos/recebidos")
    public List<AgendamentoResponse> recebidos(@AuthenticationPrincipal Jwt jwt) {
        return agenda.recebidos(UUID.fromString(jwt.getSubject()));
    }

    @PutMapping("/agendamentos/{id}/aprovar")
    public AgendamentoResponse aprovar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return agenda.decidir(UUID.fromString(jwt.getSubject()), id, true);
    }

    @PutMapping("/agendamentos/{id}/pagamento")
    public AgendamentoResponse pagar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return agenda.pagar(UUID.fromString(jwt.getSubject()), id);
    }

    @PutMapping("/agendamentos/{id}/recusar")
    public AgendamentoResponse recusar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return agenda.decidir(UUID.fromString(jwt.getSubject()), id, false);
    }
}
