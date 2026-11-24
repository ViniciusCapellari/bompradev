package br.edu.atitus.bompradev.auth.servicos;

import br.edu.atitus.bompradev.auth.dtos.CadastroRequest;
import br.edu.atitus.bompradev.auth.entidades.TipoUsuario;
import br.edu.atitus.bompradev.auth.entidades.Usuario;
import br.edu.atitus.bompradev.auth.repositorios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastroServiceTest {
    @Mock
    private UsuarioRepository usuarioRepository;
    private BCryptPasswordEncoder encoder;
    private CadastroService cadastroService;

    @BeforeEach
    void preparar() {
        encoder = new BCryptPasswordEncoder(4);
        cadastroService = new CadastroService(usuarioRepository, encoder);
    }

    @Test
    void cadastraUsuarioComSenhaProtegida() {
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));
        var resposta = cadastroService.cadastrar(new CadastroRequest("Pessoa", "Pessoa@exemplo.com", "senha123", false));

        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());
        assertEquals(TipoUsuario.USER, resposta.tipo());
        assertEquals("pessoa@exemplo.com", resposta.email());
        assertNotEquals("senha123", captor.getValue().getSenhaHash());
        assertTrue(encoder.matches("senha123", captor.getValue().getSenhaHash()));
    }

    @Test
    void cadastraProviderQuandoOpcaoMarcada() {
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));
        var resposta = cadastroService.cadastrar(new CadastroRequest("Pessoa", "pessoa@exemplo.com", "senha123", true));
        assertEquals(TipoUsuario.PROVIDER, resposta.tipo());
    }

    @Test
    void rejeitaEmailJaCadastrado() {
        when(usuarioRepository.existsByEmailIgnoreCase("pessoa@exemplo.com")).thenReturn(true);
        var erro = assertThrows(ResponseStatusException.class, () -> cadastroService.cadastrar(
                new CadastroRequest("Pessoa", "PESSOA@exemplo.com", "senha123", false)));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejeitaSenhaMaiorQueLimiteEmBytesDoBcrypt() {
        var erro = assertThrows(ResponseStatusException.class, () -> cadastroService.cadastrar(
                new CadastroRequest("Pessoa", "pessoa@exemplo.com", "á".repeat(40), false)));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verifyNoInteractions(usuarioRepository);
    }
}
