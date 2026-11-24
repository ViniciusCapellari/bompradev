package br.edu.atitus.bompradev.scheduling.repositorios;

import br.edu.atitus.bompradev.scheduling.entidades.Agendamento;
import br.edu.atitus.bompradev.scheduling.entidades.SituacaoAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {
    List<Agendamento> findByProviderIdAndSituacaoInAndInicioLessThanAndFimGreaterThan(
            UUID providerId, Collection<SituacaoAgendamento> situacoes, Instant fim, Instant inicio);
    List<Agendamento> findByUserIdOrderByInicioDesc(UUID userId);
    List<Agendamento> findByProviderIdOrderByInicioDesc(UUID providerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agendamento a where a.id = :id")
    Optional<Agendamento> buscarParaDecisao(UUID id);
}
