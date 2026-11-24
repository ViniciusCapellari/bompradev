package br.edu.atitus.bompradev.scheduling.servicos;

import br.edu.atitus.bompradev.scheduling.dtos.DisponibilidadeRequest;
import br.edu.atitus.bompradev.scheduling.entidades.Disponibilidade;
import br.edu.atitus.bompradev.scheduling.repositorios.DisponibilidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTest {
    @Mock private DisponibilidadeRepository repositorio;
    @Mock private JdbcTemplate jdbc;
    private DisponibilidadeService agenda;
    private final UUID providerId = UUID.randomUUID();
    private final LocalTime inicio = LocalTime.of(9, 0);
    private final LocalTime fim = LocalTime.of(17, 0);
    private final DisponibilidadeRequest dados = new DisponibilidadeRequest((short) 1, inicio, fim);

    @BeforeEach
    void preparar() { agenda = new DisponibilidadeService(repositorio, jdbc); }

    @Test
    void cadastraPeriodoDoProvider() {
        when(repositorio.save(any(Disponibilidade.class))).thenAnswer(chamada -> chamada.getArgument(0));
        var resultado = agenda.cadastrar(providerId, dados);
        assertEquals((short) 1, resultado.diaSemana());
        assertEquals(inicio, resultado.horaInicio());
        assertEquals(fim, resultado.horaFim());
    }

    @Test
    void rejeitaFimAntesDoInicio() {
        var erro = assertThrows(ResponseStatusException.class, () -> agenda.cadastrar(providerId,
                new DisponibilidadeRequest((short) 1, fim, inicio)));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verifyNoInteractions(repositorio, jdbc);
    }

    @Test
    void rejeitaPeriodoSobreposto() {
        when(repositorio.existsByProviderIdAndDiaSemanaAndAtivaTrueAndHoraInicioLessThanAndHoraFimGreaterThan(
                providerId, (short) 1, fim, inicio)).thenReturn(true);
        var erro = assertThrows(ResponseStatusException.class, () -> agenda.cadastrar(providerId, dados));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
        verify(repositorio, never()).save(any());
    }

    @Test
    void naoRemovePeriodoDeOutroProvider() {
        var id = UUID.randomUUID();
        var disponibilidade = mock(Disponibilidade.class);
        when(disponibilidade.getProviderId()).thenReturn(UUID.randomUUID());
        when(repositorio.findById(id)).thenReturn(Optional.of(disponibilidade));
        var erro = assertThrows(ResponseStatusException.class, () -> agenda.remover(providerId, id));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        verify(repositorio, never()).delete(any());
    }

    @Test
    void removePeriodoDoProprioProvider() {
        var id = UUID.randomUUID();
        var disponibilidade = mock(Disponibilidade.class);
        when(disponibilidade.getProviderId()).thenReturn(providerId);
        when(repositorio.findById(id)).thenReturn(Optional.of(disponibilidade));
        agenda.remover(providerId, id);
        verify(repositorio).delete(disponibilidade);
    }
}
