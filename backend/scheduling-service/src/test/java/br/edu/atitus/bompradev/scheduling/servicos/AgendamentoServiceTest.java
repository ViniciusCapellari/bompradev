package br.edu.atitus.bompradev.scheduling.servicos;

import br.edu.atitus.bompradev.scheduling.clientes.CatalogoClient;
import br.edu.atitus.bompradev.scheduling.dtos.AgendamentoRequest;
import br.edu.atitus.bompradev.scheduling.dtos.ServicoCatalogo;
import br.edu.atitus.bompradev.scheduling.entidades.Agendamento;
import br.edu.atitus.bompradev.scheduling.entidades.Disponibilidade;
import br.edu.atitus.bompradev.scheduling.entidades.SituacaoAgendamento;
import br.edu.atitus.bompradev.scheduling.entidades.SituacaoPagamento;
import br.edu.atitus.bompradev.scheduling.repositorios.AgendamentoRepository;
import br.edu.atitus.bompradev.scheduling.repositorios.DisponibilidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {
    @Mock private CatalogoClient catalogo;
    @Mock private DisponibilidadeRepository disponibilidades;
    @Mock private AgendamentoRepository agendamentos;
    @Mock private JdbcTemplate jdbc;
    private AgendamentoService agenda;
    private final UUID servicoId = UUID.randomUUID();
    private final UUID providerId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final LocalDate data = LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(7);
    private final ZoneId fuso = ZoneId.of("America/Sao_Paulo");

    @BeforeEach
    void preparar() { agenda = new AgendamentoService(catalogo, disponibilidades, agendamentos, jdbc); }

    private void prepararPeriodo(int duracao) {
        when(catalogo.consultar(servicoId)).thenReturn(new ServicoCatalogo(servicoId, providerId, duracao, new BigDecimal("100.00")));
        var periodo = new Disponibilidade(providerId, (short) (data.getDayOfWeek().getValue() % 7),
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        when(disponibilidades.findByProviderIdAndDiaSemanaAndAtivaTrueOrderByHoraInicioAsc(providerId,
                (short) (data.getDayOfWeek().getValue() % 7))).thenReturn(List.of(periodo));
    }

    @Test
    void divideDisponibilidadePelaDuracaoDoServico() {
        prepararPeriodo(60);
        assertEquals(List.of(LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0)), agenda.horarios(servicoId, data));
    }

    @Test
    void removeTodosOsHorariosQueSobrepoemUmaReserva() {
        prepararPeriodo(60);
        var ocupado = new Agendamento(servicoId, UUID.randomUUID(), providerId,
                data.atTime(9, 30).atZone(fuso).toInstant(), data.atTime(10, 30).atZone(fuso).toInstant(), new BigDecimal("100.00"));
        when(agendamentos.findByProviderIdAndSituacaoInAndInicioLessThanAndFimGreaterThan(eq(providerId), anyCollection(), any(), any()))
                .thenReturn(List.of(ocupado));
        assertEquals(List.of(LocalTime.of(11, 0)), agenda.horarios(servicoId, data));
    }

    @Test
    void criaSolicitacaoPendenteComDadosDoCatalogo() {
        prepararPeriodo(60);
        when(agendamentos.save(any(Agendamento.class))).thenAnswer(chamada -> chamada.getArgument(0));
        var resultado = agenda.solicitar(userId, new AgendamentoRequest(servicoId, data, LocalTime.of(9, 0)));
        assertEquals(providerId, resultado.providerId());
        assertEquals(SituacaoAgendamento.PENDENTE, resultado.situacao());
        assertEquals(new BigDecimal("100.00"), resultado.valorContratado());
        assertEquals(data.atTime(9, 0).atZone(fuso).toInstant(), resultado.inicio());
        assertEquals(data.atTime(10, 0).atZone(fuso).toInstant(), resultado.fim());
    }

    @Test
    void naoAceitaHorarioForaDaGrade() {
        prepararPeriodo(60);
        var erro = assertThrows(ResponseStatusException.class, () -> agenda.solicitar(userId,
                new AgendamentoRequest(servicoId, data, LocalTime.of(9, 30))));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
        verify(agendamentos, never()).save(any());
    }

    @Test
    void naoOfereceDatasPassadas() {
        when(catalogo.consultar(servicoId)).thenReturn(new ServicoCatalogo(servicoId, providerId, 60, new BigDecimal("100.00")));
        assertTrue(agenda.horarios(servicoId, LocalDate.now(fuso).minusDays(1)).isEmpty());
        verifyNoInteractions(disponibilidades, agendamentos);
    }

    @Test
    void naoEncaixaServicoMaiorQueOPeriodo() {
        prepararPeriodo(181);
        assertTrue(agenda.horarios(servicoId, data).isEmpty());
    }

    @Test
    void prestadorPodeAprovarSolicitacaoPendente() {
        var id = UUID.randomUUID();
        var pedido = new Agendamento(servicoId, userId, providerId, data.atTime(9, 0).atZone(fuso).toInstant(), data.atTime(10, 0).atZone(fuso).toInstant(), new BigDecimal("100.00"));
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));
        assertEquals(SituacaoAgendamento.APROVADO, agenda.decidir(providerId, id, true).situacao());
    }

    @Test
    void prestadorPodeRecusarSolicitacaoPendente() {
        var id = UUID.randomUUID();
        var pedido = new Agendamento(servicoId, userId, providerId, data.atTime(9, 0).atZone(fuso).toInstant(), data.atTime(10, 0).atZone(fuso).toInstant(), new BigDecimal("100.00"));
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));
        assertEquals(SituacaoAgendamento.RECUSADO, agenda.decidir(providerId, id, false).situacao());
    }

    @Test
    void outroPrestadorNaoPodeResponder() {
        var id = UUID.randomUUID();
        var pedido = new Agendamento(servicoId, userId, providerId, data.atTime(9, 0).atZone(fuso).toInstant(), data.atTime(10, 0).atZone(fuso).toInstant(), new BigDecimal("100.00"));
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));
        var erro = assertThrows(ResponseStatusException.class, () -> agenda.decidir(UUID.randomUUID(), id, true));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        assertEquals(SituacaoAgendamento.PENDENTE, pedido.getSituacao());
    }

    @Test
    void naoPermiteResponderDuasVezes() {
        var id = UUID.randomUUID();
        var pedido = new Agendamento(servicoId, userId, providerId, data.atTime(9, 0).atZone(fuso).toInstant(), data.atTime(10, 0).atZone(fuso).toInstant(), new BigDecimal("100.00"));
        pedido.decidir(SituacaoAgendamento.APROVADO);
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));
        var erro = assertThrows(ResponseStatusException.class, () -> agenda.decidir(providerId, id, false));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
        assertEquals(SituacaoAgendamento.APROVADO, pedido.getSituacao());
    }

    private Agendamento agendamentoComValor(BigDecimal valor) {
        return new Agendamento(servicoId, userId, providerId, data.atTime(9, 0).atZone(fuso).toInstant(),
                data.atTime(10, 0).atZone(fuso).toInstant(), valor);
    }

    @Test
    void clienteRegistraPagamentoSimuladoAposAprovacao() {
        var id = UUID.randomUUID();
        var pedido = agendamentoComValor(new BigDecimal("100.00"));
        pedido.decidir(SituacaoAgendamento.APROVADO);
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));

        var resultado = agenda.pagar(userId, id);

        assertEquals(SituacaoPagamento.PAGO, resultado.situacaoPagamento());
        assertEquals(SituacaoAgendamento.APROVADO, resultado.situacao());
        assertEquals(new BigDecimal("100.00"), resultado.valorContratado());
    }

    @Test
    void naoPermitePagamentoAntesDaAprovacao() {
        var id = UUID.randomUUID();
        var pedido = agendamentoComValor(new BigDecimal("100.00"));
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));

        var erro = assertThrows(ResponseStatusException.class, () -> agenda.pagar(userId, id));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
        assertEquals(SituacaoPagamento.NAO_REALIZADO, pedido.getSituacaoPagamento());
    }

    @Test
    void outroUsuarioNaoPodePagarAgendamento() {
        var id = UUID.randomUUID();
        var pedido = agendamentoComValor(new BigDecimal("100.00"));
        pedido.decidir(SituacaoAgendamento.APROVADO);
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));

        var erro = assertThrows(ResponseStatusException.class, () -> agenda.pagar(UUID.randomUUID(), id));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        assertEquals(SituacaoPagamento.NAO_REALIZADO, pedido.getSituacaoPagamento());
    }

    @Test
    void naoRegistraMesmoPagamentoDuasVezes() {
        var id = UUID.randomUUID();
        var pedido = agendamentoComValor(new BigDecimal("100.00"));
        pedido.decidir(SituacaoAgendamento.APROVADO);
        pedido.registrarPagamento();
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));

        var erro = assertThrows(ResponseStatusException.class, () -> agenda.pagar(userId, id));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
    }

    @Test
    void naoInventaValorDeAgendamentoAntigo() {
        var id = UUID.randomUUID();
        var pedido = agendamentoComValor(null);
        pedido.decidir(SituacaoAgendamento.APROVADO);
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));

        var erro = assertThrows(ResponseStatusException.class, () -> agenda.pagar(userId, id));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
        assertEquals(SituacaoPagamento.NAO_REALIZADO, pedido.getSituacaoPagamento());
    }

    @Test
    void naoPermitePagamentoDeAgendamentoRecusado() {
        var id = UUID.randomUUID();
        var pedido = agendamentoComValor(new BigDecimal("100.00"));
        pedido.decidir(SituacaoAgendamento.RECUSADO);
        when(agendamentos.buscarParaDecisao(id)).thenReturn(Optional.of(pedido));

        var erro = assertThrows(ResponseStatusException.class, () -> agenda.pagar(userId, id));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
    }
}
