package br.edu.atitus.bompradev.auth.servicos;

import br.edu.atitus.bompradev.auth.componentes.JwtUtil;
import br.edu.atitus.bompradev.auth.dtos.PerfilRequest;
import br.edu.atitus.bompradev.auth.entidades.TipoUsuario;
import br.edu.atitus.bompradev.auth.entidades.Usuario;
import br.edu.atitus.bompradev.auth.repositorios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {
    @Mock private UsuarioRepository repositorio;
    @Mock private JwtUtil jwtUtil;
    private PerfilService service;
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void preparar() {
        service = new PerfilService(repositorio, jwtUtil);
    }

    @Test
    void atualizaDadosPublicosDaPropriaConta() {
        var usuario = new Usuario("Nome", "pessoa@exemplo.com", "hash", TipoUsuario.USER);
        when(repositorio.findById(id)).thenReturn(Optional.of(usuario));

        var resposta = service.atualizar(id, new PerfilRequest(" Nome atualizado ", " Minha bio ", " portfolio "));

        assertEquals("Nome atualizado", resposta.nome());
        assertEquals("Minha bio", resposta.biografia());
        assertEquals("portfolio", resposta.portfolio());
        assertEquals(TipoUsuario.USER, resposta.tipo());
    }

    @Test
    void ativaProviderEEmiteNovoToken() {
        var usuario = new Usuario("Nome", "pessoa@exemplo.com", "hash", TipoUsuario.USER);
        when(repositorio.findById(id)).thenReturn(Optional.of(usuario));
        when(jwtUtil.gerarToken(usuario, 3600)).thenReturn("novo-token");

        var resposta = service.tornarProvider(id);

        assertEquals(TipoUsuario.PROVIDER, usuario.getTipo());
        assertEquals(TipoUsuario.PROVIDER, resposta.usuario().tipo());
        assertEquals("novo-token", resposta.token());
    }

    @Test
    void naoPermiteQueAdministradorAtivePerfilDeProvider() {
        var usuario = new Usuario("Admin", "admin@exemplo.com", "hash", TipoUsuario.ADMIN);
        when(repositorio.findById(id)).thenReturn(Optional.of(usuario));
        var erro = assertThrows(ResponseStatusException.class, () -> service.tornarProvider(id));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void naoExibePerfilInexistente() {
        when(repositorio.findById(id)).thenReturn(Optional.empty());
        var erro = assertThrows(ResponseStatusException.class, () -> service.consultarPublico(id));
        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
    }
}
