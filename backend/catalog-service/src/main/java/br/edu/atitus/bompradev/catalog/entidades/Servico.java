package br.edu.atitus.bompradev.catalog.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "servicos")
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoServico situacao;

    protected Servico() {
    }

    public Servico(UUID providerId, Categoria categoria, String titulo, String descricao,
                   BigDecimal valor, Integer duracaoMinutos) {
        this.providerId = providerId;
        this.situacao = SituacaoServico.APROVADO;
        atualizar(categoria, titulo, descricao, valor, duracaoMinutos);
    }

    public void atualizar(Categoria categoria, String titulo, String descricao,
                          BigDecimal valor, Integer duracaoMinutos) {
        this.categoria = categoria;
        this.titulo = titulo;
        this.descricao = descricao;
        this.valor = valor;
        this.duracaoMinutos = duracaoMinutos;
    }

    public void inativar() {
        this.situacao = SituacaoServico.INATIVO;
    }

    public void alterarImagem(String url) {
        this.imagemUrl = url;
    }

    public UUID getId() { return id; }
    public UUID getProviderId() { return providerId; }
    public Categoria getCategoria() { return categoria; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public String getImagemUrl() { return imagemUrl; }
    public SituacaoServico getSituacao() { return situacao; }
}
