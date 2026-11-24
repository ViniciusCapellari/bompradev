package br.edu.atitus.bompradev.auth.configs;

import br.edu.atitus.bompradev.auth.componentes.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class ConfigJwt {
    public static final String EMISSOR = "bompradev-auth";

    @Bean
    SecretKey chaveJwt(@Value("${JWT_SECRET}") String segredo) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET deve ter pelo menos 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(JwtUtil jwtUtil) {
        return jwtUtil::validarToken;
    }
}
