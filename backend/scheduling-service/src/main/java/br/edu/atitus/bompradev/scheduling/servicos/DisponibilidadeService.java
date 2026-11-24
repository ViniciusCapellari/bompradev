package br.edu.atitus.bompradev.scheduling.servicos;

import br.edu.atitus.bompradev.scheduling.dtos.DisponibilidadeRequest;
import br.edu.atitus.bompradev.scheduling.dtos.DisponibilidadeResponse;
import br.edu.atitus.bompradev.scheduling.entidades.Disponibilidade;
import br.edu.atitus.bompradev.scheduling.repositorios.DisponibilidadeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
public class DisponibilidadeService {
    private final DisponibilidadeRepository disponibilidades;
    private final JdbcTemplate jdbc;

    public DisponibilidadeService(DisponibilidadeRepository disponibilidades, JdbcTemplate jdbc) {
        this.disponibilidades = disponibilidades;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeResponse> listar(UUID providerId) {
        return disponibilidades.findByProviderIdAndAtivaTrueOrderByDiaSemanaAscHoraInicioAsc(providerId)
                .stream().map(DisponibilidadeResponse::de).toList();
    }

    @Transactional
    public DisponibilidadeResponse cadastrar(UUID providerId, DisponibilidadeRequest dados) {
        if (!dados.horaInicio().isBefore(dados.horaFim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horário de início deve ser anterior ao de fim");
        }
        jdbc.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> null, providerId.toString());
        if (disponibilidades.existsByProviderIdAndDiaSemanaAndAtivaTrueAndHoraInicioLessThanAndHoraFimGreaterThan(
                providerId, dados.diaSemana(), dados.horaFim(), dados.horaInicio())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe disponibilidade nesse período");
        }
        return DisponibilidadeResponse.de(disponibilidades.save(new Disponibilidade(providerId, dados.diaSemana(),
                dados.horaInicio(), dados.horaFim())));
    }

    @Transactional
    public void remover(UUID providerId, UUID id) {
        var disponibilidade = disponibilidades.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disponibilidade não encontrada"));
        if (!disponibilidade.getProviderId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Disponibilidade pertence a outro prestador");
        }
        jdbc.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> null, providerId.toString());
        disponibilidades.delete(disponibilidade);
    }
}
