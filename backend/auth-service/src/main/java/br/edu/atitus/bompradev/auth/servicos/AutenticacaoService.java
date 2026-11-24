package br.edu.atitus.bompradev.auth.servicos;

import br.edu.atitus.bompradev.auth.componentes.JwtUtil;
import br.edu.atitus.bompradev.auth.dtos.LoginRequest;
import br.edu.atitus.bompradev.auth.dtos.LoginResponse;
import br.edu.atitus.bompradev.auth.dtos.UsuarioResponse;
import br.edu.atitus.bompradev.auth.entidades.Usuario;
import br.edu.atitus.bompradev.auth.repositorios.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Service
public class AutenticacaoService {
    private static final long DURACAO_TOKEN_SEGUNDOS = 3600;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AutenticacaoService(UsuarioRepository usuarioRepository, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse entrar(LoginRequest dados) {
        if (dados.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw credenciaisInvalidas();
        }
        String email = dados.email().strip().toLowerCase(Locale.ROOT);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, dados.senha()));
        } catch (AuthenticationException e) {
            throw credenciaisInvalidas();
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .filter(Usuario::isAtivo)
                .orElseThrow(AutenticacaoService::credenciaisInvalidas);
        String token = jwtUtil.gerarToken(usuario, DURACAO_TOKEN_SEGUNDOS);
        return new LoginResponse(token, "Bearer", DURACAO_TOKEN_SEGUNDOS, UsuarioResponse.de(usuario));
    }

    public UsuarioResponse obterUsuario(String identificador) {
        try {
            Usuario usuario = usuarioRepository.findById(UUID.fromString(identificador))
                    .filter(Usuario::isAtivo)
                    .orElseThrow(AutenticacaoService::credenciaisInvalidas);
            return UsuarioResponse.de(usuario);
        } catch (IllegalArgumentException e) {
            throw credenciaisInvalidas();
        }
    }

    private static ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }
}
