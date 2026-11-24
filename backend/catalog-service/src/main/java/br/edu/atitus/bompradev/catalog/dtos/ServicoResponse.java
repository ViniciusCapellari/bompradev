package br.edu.atitus.bompradev.catalog.dtos;

import br.edu.atitus.bompradev.catalog.entidades.Servico;
import br.edu.atitus.bompradev.catalog.entidades.SituacaoServico;
import java.math.BigDecimal;
import java.util.UUID;

public record ServicoResponse(UUID id, UUID providerId, CategoriaResponse categoria, String titulo,
                              String descricao, BigDecimal valor, Integer duracaoMinutos,
                              String imagemUrl, SituacaoServico situacao) {
    public static ServicoResponse de(Servico servico) {
        return new ServicoResponse(servico.getId(), servico.getProviderId(), CategoriaResponse.de(servico.getCategoria()),
                servico.getTitulo(), servico.getDescricao(), servico.getValor(), servico.getDuracaoMinutos(),
                servico.getImagemUrl(), servico.getSituacao());
    }
}
