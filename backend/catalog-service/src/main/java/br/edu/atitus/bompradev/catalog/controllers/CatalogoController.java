package br.edu.atitus.bompradev.catalog.controllers;

import br.edu.atitus.bompradev.catalog.dtos.CategoriaResponse;
import br.edu.atitus.bompradev.catalog.dtos.ServicoRequest;
import br.edu.atitus.bompradev.catalog.dtos.ServicoResponse;
import br.edu.atitus.bompradev.catalog.servicos.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/catalogo")
public class CatalogoController {
    private final CatalogoService catalogo;

    public CatalogoController(CatalogoService catalogo) { this.catalogo = catalogo; }

    @GetMapping("/categorias")
    public List<CategoriaResponse> categorias() { return catalogo.listarCategorias(); }

    @GetMapping("/servicos")
    public List<ServicoResponse> listar(@RequestParam(required = false) UUID categoriaId) {
        return catalogo.listar(categoriaId);
    }

    @GetMapping("/servicos/{id}")
    public ServicoResponse consultar(@PathVariable UUID id) { return catalogo.consultar(id); }

    @GetMapping("/meus-servicos")
    public List<ServicoResponse> meusServicos(@AuthenticationPrincipal Jwt jwt) {
        return catalogo.listarDoProvider(UUID.fromString(jwt.getSubject()));
    }

    @PostMapping("/servicos")
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResponse cadastrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ServicoRequest dados) {
        return catalogo.cadastrar(UUID.fromString(jwt.getSubject()), dados);
    }

    @PutMapping("/servicos/{id}")
    public ServicoResponse atualizar(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt,
                                      @Valid @RequestBody ServicoRequest dados) {
        return catalogo.atualizar(id, UUID.fromString(jwt.getSubject()), dados);
    }
    @DeleteMapping("/servicos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        catalogo.inativar(id, UUID.fromString(jwt.getSubject()));
    }

    @PostMapping("/servicos/{id}/imagem")
    public ServicoResponse enviarImagem(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt,
                                       @RequestParam("imagem") MultipartFile imagem) {
        return catalogo.alterarImagem(id, UUID.fromString(jwt.getSubject()), imagem);
    }
}
