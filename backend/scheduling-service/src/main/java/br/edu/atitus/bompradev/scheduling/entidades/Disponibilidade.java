package br.edu.atitus.bompradev.scheduling.entidades;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "disponibilidades")
public class Disponibilidade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "provider_id", nullable = false)
    private UUID providerId;
    @Column(name = "dia_semana", nullable = false)
    private Short diaSemana;
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;
    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;
    @Column(nullable = false)
    private boolean ativa = true;

    protected Disponibilidade() {}

    public Disponibilidade(UUID providerId, short diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        this.providerId = providerId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public UUID getId() { return id; }
    public UUID getProviderId() { return providerId; }
    public Short getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
}
