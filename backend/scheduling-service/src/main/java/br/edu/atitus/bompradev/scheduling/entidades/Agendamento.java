package br.edu.atitus.bompradev.scheduling.entidades;

import jakarta.persistence.*;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "agendamentos")
public class Agendamento {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "servico_id", nullable = false)
    private UUID servicoId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "provider_id", nullable = false)
    private UUID providerId;
    @Column(nullable = false)
    private Instant inicio;
    @Column(nullable = false)
    private Instant fim;
    @Column(name = "valor_contratado", precision = 10, scale = 2)
    private BigDecimal valorContratado;
    @Column(name = "atualizado_em", insertable = false, nullable = false)
    private Instant atualizadoEm;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private SituacaoAgendamento situacao = SituacaoAgendamento.PENDENTE;
    @Enumerated(EnumType.STRING) @Column(name = "situacao_pagamento", nullable = false, length = 20)
    private SituacaoPagamento situacaoPagamento = SituacaoPagamento.NAO_REALIZADO;

    protected Agendamento() {}
    public Agendamento(UUID servicoId, UUID userId, UUID providerId, Instant inicio, Instant fim, BigDecimal valorContratado) {
        this.servicoId = servicoId;
        this.userId = userId;
        this.providerId = providerId;
        this.inicio = inicio;
        this.fim = fim;
        this.valorContratado = valorContratado;
    }

    public UUID getId() { return id; }
    public UUID getServicoId() { return servicoId; }
    public UUID getUserId() { return userId; }
    public UUID getProviderId() { return providerId; }
    public Instant getInicio() { return inicio; }
    public Instant getFim() { return fim; }
    public BigDecimal getValorContratado() { return valorContratado; }
    public SituacaoAgendamento getSituacao() { return situacao; }
    public SituacaoPagamento getSituacaoPagamento() { return situacaoPagamento; }
    public void decidir(SituacaoAgendamento decisao) { this.situacao = decisao; }
    public void registrarPagamento() { this.situacaoPagamento = SituacaoPagamento.PAGO; }
    @PreUpdate
    protected void atualizarData() { atualizadoEm = Instant.now(); }
}
