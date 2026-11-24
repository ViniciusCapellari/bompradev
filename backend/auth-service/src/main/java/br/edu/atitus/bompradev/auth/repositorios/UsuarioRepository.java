package br.edu.atitus.bompradev.auth.repositorios;

import br.edu.atitus.bompradev.auth.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);
}
