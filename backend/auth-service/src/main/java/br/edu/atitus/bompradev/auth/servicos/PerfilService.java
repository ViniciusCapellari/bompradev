package br.edu.atitus.bompradev.auth.servicos;

import br.edu.atitus.bompradev.auth.componentes.JwtUtil;
import br.edu.atitus.bompradev.auth.dtos.LoginResponse;
import br.edu.atitus.bompradev.auth.dtos.PerfilPublicoResponse;
import br.edu.atitus.bompradev.auth.dtos.PerfilRequest;
import br.edu.atitus.bompradev.auth.dtos.UsuarioResponse;
import br.edu.atitus.bompradev.auth.entidades.TipoUsuario;
import br.edu.atitus.bompradev.auth.entidades.Usuario;
import br.edu.atitus.bompradev.auth.repositorios.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class PerfilService {
    private final UsuarioRepository repositorio;
    private final JwtUtil jwtUtil;

    public PerfilService(UsuarioRepository repositorio, JwtUtil jwtUtil) {
        this.repositorio = repositorio;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(readOnly = true)
    public PerfilPublicoResponse consultarPublico(UUID id) {
        return PerfilPublicoResponse.de(repositorio.findById(id).filter(Usuario::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado")));
    }

    @Transactional
    public UsuarioResponse atualizar(UUID id, PerfilRequest dados) {
        Usuario usuario = buscarAtivo(id);
        usuario.atualizarPerfil(dados.nome().strip(), limpar(dados.biografia()), limpar(dados.portfolio()));
        return UsuarioResponse.de(usuario);
    }

    @Transactional
    public LoginResponse tornarProvider(UUID id) {
        Usuario usuario = buscarAtivo(id);
        if (usuario.getTipo() != TipoUsuario.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente usuários podem ativar o perfil de prestador");
        }
        usuario.tornarProvider();
        return new LoginResponse(jwtUtil.gerarToken(usuario, 3600), "Bearer", 3600, UsuarioResponse.de(usuario));
    }

    private Usuario buscarAtivo(UUID id) {
        return repositorio.findById(id).filter(Usuario::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta indisponível"));
    }

    private String limpar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
