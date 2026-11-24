package br.edu.atitus.bompradev.auth.servicos;

import br.edu.atitus.bompradev.auth.dtos.CadastroRequest;
import br.edu.atitus.bompradev.auth.dtos.UsuarioResponse;
import br.edu.atitus.bompradev.auth.entidades.TipoUsuario;
import br.edu.atitus.bompradev.auth.entidades.Usuario;
import br.edu.atitus.bompradev.auth.repositorios.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class CadastroService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponse cadastrar(CadastroRequest dados) {
        String email = dados.email().strip().toLowerCase(Locale.ROOT);
        if (dados.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha excede o tamanho permitido");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        TipoUsuario tipo = dados.provider() ? TipoUsuario.PROVIDER : TipoUsuario.USER;
        Usuario usuario = new Usuario(dados.nome().strip(), email, passwordEncoder.encode(dados.senha()), tipo);
        try {
            return UsuarioResponse.de(usuarioRepository.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException e) {
            if (usuarioRepository.existsByEmailIgnoreCase(email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            }
            throw e;
        }
    }
}
