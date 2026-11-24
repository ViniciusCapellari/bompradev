package br.edu.atitus.bompradev.auth.servicos;

import br.edu.atitus.bompradev.auth.componentes.JwtUtil;
import br.edu.atitus.bompradev.auth.dtos.LoginRequest;
import br.edu.atitus.bompradev.auth.entidades.TipoUsuario;
import br.edu.atitus.bompradev.auth.entidades.Usuario;
import br.edu.atitus.bompradev.auth.repositorios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {
    @Mock private UsuarioRepository repositorio;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    private AutenticacaoService autenticacao;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        autenticacao = new AutenticacaoService(repositorio, authenticationManager, jwtUtil);
        usuario = new Usuario("Pessoa", "pessoa@exemplo.com", "hash", TipoUsuario.PROVIDER);
        ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
    }

    @Test
    void autenticaUsuarioEDevolveToken() {
        when(repositorio.findByEmailIgnoreCase("pessoa@exemplo.com")).thenReturn(Optional.of(usuario));
        when(jwtUtil.gerarToken(usuario, 3600)).thenReturn("token");
        var resposta = autenticacao.entrar(new LoginRequest("PESSOA@exemplo.com", "senha123"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("token", resposta.token());
        assertEquals("Bearer", resposta.tipoToken());
        assertEquals(3600, resposta.expiraEmSegundos());
        assertEquals(TipoUsuario.PROVIDER, resposta.usuario().tipo());
    }

    @Test
    void rejeitaCredenciaisIncorretasSemEmitirToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Inválido"));
        var erro = assertThrows(ResponseStatusException.class,
                () -> autenticacao.entrar(new LoginRequest("pessoa@exemplo.com", "senhaerrada")));
        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());
        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(repositorio);
    }

    @Test
    void rejeitaUsuarioInativoAposAutenticacao() {
        ReflectionTestUtils.setField(usuario, "ativo", false);
        when(repositorio.findByEmailIgnoreCase("pessoa@exemplo.com")).thenReturn(Optional.of(usuario));
        var erro = assertThrows(ResponseStatusException.class,
                () -> autenticacao.entrar(new LoginRequest("pessoa@exemplo.com", "senha123")));
        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void consultaUsuarioAtivoPeloIdentificador() {
        when(repositorio.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        assertEquals(usuario.getId(), autenticacao.obterUsuario(usuario.getId().toString()).id());
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ResponseStatusException.class,
                () -> autenticacao.obterUsuario("identificador-invalido")).getStatusCode());
    }
}
