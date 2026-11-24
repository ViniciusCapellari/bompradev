package br.edu.atitus.bompradev.catalog.servicos;

import br.edu.atitus.bompradev.catalog.dtos.ServicoRequest;
import br.edu.atitus.bompradev.catalog.entidades.Categoria;
import br.edu.atitus.bompradev.catalog.entidades.Servico;
import br.edu.atitus.bompradev.catalog.entidades.SituacaoServico;
import br.edu.atitus.bompradev.catalog.repositorios.CategoriaRepository;
import br.edu.atitus.bompradev.catalog.repositorios.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {
    @Mock private CategoriaRepository categorias;
    @Mock private ServicoRepository servicos;
    @Mock private ImagemService imagens;
    private CatalogoService catalogo;
    private final UUID providerId = UUID.randomUUID();
    private final UUID categoriaId = UUID.randomUUID();
    private final ServicoRequest dados = new ServicoRequest(categoriaId, " Site ", " Descrição ",
            new BigDecimal("120.00"), 60);

    @BeforeEach
    void preparar() { catalogo = new CatalogoService(categorias, servicos, imagens); }

    @Test
    void publicaServicoDoProviderImediatamente() {
        var categoria = mock(Categoria.class);
        when(categoria.isAtiva()).thenReturn(true);
        when(categorias.findById(categoriaId)).thenReturn(Optional.of(categoria));
        when(servicos.save(any(Servico.class))).thenAnswer(chamada -> chamada.getArgument(0));

        var resultado = catalogo.cadastrar(providerId, dados);

        assertEquals(providerId, resultado.providerId());
        assertEquals("Site", resultado.titulo());
        assertEquals(SituacaoServico.APROVADO, resultado.situacao());
    }

    @Test
    void naoAceitaCategoriaInativa() {
        var categoria = mock(Categoria.class);
        when(categorias.findById(categoriaId)).thenReturn(Optional.of(categoria));
        var erro = assertThrows(ResponseStatusException.class, () -> catalogo.cadastrar(providerId, dados));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verifyNoInteractions(servicos);
    }

    @Test
    void naoPermiteAlterarServicoDeOutroProvider() {
        var id = UUID.randomUUID();
        var servico = mock(Servico.class);
        when(servico.getProviderId()).thenReturn(UUID.randomUUID());
        when(servicos.findById(id)).thenReturn(Optional.of(servico));

        var erro = assertThrows(ResponseStatusException.class, () -> catalogo.atualizar(id, providerId, dados));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        verifyNoInteractions(categorias);
    }

    @Test
    void naoPermiteEditarServicoIndisponivel() {
        var id = UUID.randomUUID();
        var servico = mock(Servico.class);
        when(servico.getProviderId()).thenReturn(providerId);
        when(servico.getSituacao()).thenReturn(SituacaoServico.REJEITADO);
        when(servicos.findById(id)).thenReturn(Optional.of(servico));

        var erro = assertThrows(ResponseStatusException.class, () -> catalogo.atualizar(id, providerId, dados));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
    }
    @Test
    void inativaServicoDoProprioProvider() {
        var id = UUID.randomUUID();
        var servico = new Servico(providerId, mock(Categoria.class), "Site", "Descrição", BigDecimal.TEN, 60);
        when(servicos.findById(id)).thenReturn(Optional.of(servico));

        catalogo.inativar(id, providerId);

        assertEquals(SituacaoServico.INATIVO, servico.getSituacao());
        verify(servicos, never()).delete(any());
    }

    @Test
    void naoPermiteInativarServicoDeOutroProvider() {
        var id = UUID.randomUUID();
        var servico = new Servico(UUID.randomUUID(), mock(Categoria.class), "Site", "Descrição", BigDecimal.TEN, 60);
        when(servicos.findById(id)).thenReturn(Optional.of(servico));

        var erro = assertThrows(ResponseStatusException.class, () -> catalogo.inativar(id, providerId));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        assertEquals(SituacaoServico.APROVADO, servico.getSituacao());
    }

    @Test
    void naoPermiteInativarServicoJaIndisponivel() {
        var id = UUID.randomUUID();
        var servico = new Servico(providerId, mock(Categoria.class), "Site", "Descrição", BigDecimal.TEN, 60);
        servico.inativar();
        when(servicos.findById(id)).thenReturn(Optional.of(servico));

        var erro = assertThrows(ResponseStatusException.class, () -> catalogo.inativar(id, providerId));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
    }

    @Test
    void naoPermiteEnviarImagemParaServicoDeOutroProvider() {
        var id = UUID.randomUUID();
        var servico = new Servico(UUID.randomUUID(), mock(Categoria.class), "Site", "Descrição", BigDecimal.TEN, 60);
        when(servicos.findById(id)).thenReturn(Optional.of(servico));
        var imagem = new MockMultipartFile("imagem", "teste.png", "image/png", new byte[]{1});

        var erro = assertThrows(ResponseStatusException.class, () -> catalogo.alterarImagem(id, providerId, imagem));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        verifyNoInteractions(imagens);
    }
}
