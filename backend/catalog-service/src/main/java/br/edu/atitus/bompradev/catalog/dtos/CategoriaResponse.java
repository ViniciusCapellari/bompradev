package br.edu.atitus.bompradev.catalog.dtos;

import br.edu.atitus.bompradev.catalog.entidades.Categoria;
import java.util.UUID;

public record CategoriaResponse(UUID id, String nome, String descricao) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao());
    }
}
