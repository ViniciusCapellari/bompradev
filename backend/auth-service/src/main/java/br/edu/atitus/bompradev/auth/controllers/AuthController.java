package br.edu.atitus.bompradev.auth.controllers;

import br.edu.atitus.bompradev.auth.dtos.CadastroRequest;
import br.edu.atitus.bompradev.auth.dtos.LoginRequest;
import br.edu.atitus.bompradev.auth.dtos.LoginResponse;
import br.edu.atitus.bompradev.auth.dtos.UsuarioResponse;
import br.edu.atitus.bompradev.auth.servicos.AutenticacaoService;
import br.edu.atitus.bompradev.auth.servicos.CadastroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final CadastroService cadastroService;
    private final AutenticacaoService autenticacaoService;

    public AuthController(CadastroService cadastroService, AutenticacaoService autenticacaoService) {
        this.cadastroService = cadastroService;
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest dados) {
        return cadastroService.cadastrar(dados);
    }

    @PostMapping("/signin")
    public LoginResponse entrar(@Valid @RequestBody LoginRequest dados) {
        return autenticacaoService.entrar(dados);
    }

    @GetMapping("/me")
    public UsuarioResponse obterConta(@AuthenticationPrincipal Jwt jwt) {
        return autenticacaoService.obterUsuario(jwt.getSubject());
    }
}
