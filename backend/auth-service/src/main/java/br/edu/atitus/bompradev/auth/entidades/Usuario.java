package br.edu.atitus.bompradev.auth.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoUsuario tipo;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(length = 1000)
    private String biografia;

    @Column(length = 500)
    private String portfolio;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, TipoUsuario tipo) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.tipo = tipo;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public TipoUsuario getTipo() { return tipo; }
    public boolean isAtivo() { return ativo; }
    public String getBiografia() { return biografia; }
    public String getPortfolio() { return portfolio; }

    public void atualizarPerfil(String nome, String biografia, String portfolio) {
        this.nome = nome;
        this.biografia = biografia;
        this.portfolio = portfolio;
    }

    public void tornarProvider() {
        this.tipo = TipoUsuario.PROVIDER;
    }
}
