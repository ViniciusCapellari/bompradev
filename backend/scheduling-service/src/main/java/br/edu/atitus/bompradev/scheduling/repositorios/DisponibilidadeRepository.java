package br.edu.atitus.bompradev.scheduling.repositorios;

import br.edu.atitus.bompradev.scheduling.entidades.Disponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface DisponibilidadeRepository extends JpaRepository<Disponibilidade, UUID> {
    List<Disponibilidade> findByProviderIdAndAtivaTrueOrderByDiaSemanaAscHoraInicioAsc(UUID providerId);
    List<Disponibilidade> findByProviderIdAndDiaSemanaAndAtivaTrueOrderByHoraInicioAsc(UUID providerId, Short diaSemana);
    boolean existsByProviderIdAndDiaSemanaAndAtivaTrueAndHoraInicioLessThanAndHoraFimGreaterThan(
            UUID providerId, Short diaSemana, LocalTime horaFim, LocalTime horaInicio);
}
