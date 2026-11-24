package br.edu.atitus.bompradev.catalog.repositorios;

import br.edu.atitus.bompradev.catalog.entidades.Servico;
import br.edu.atitus.bompradev.catalog.entidades.SituacaoServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {
    List<Servico> findBySituacaoOrderByTituloAsc(SituacaoServico situacao);
    List<Servico> findBySituacaoAndCategoriaIdOrderByTituloAsc(SituacaoServico situacao, UUID categoriaId);
    List<Servico> findByProviderIdOrderByTituloAsc(UUID providerId);
}
