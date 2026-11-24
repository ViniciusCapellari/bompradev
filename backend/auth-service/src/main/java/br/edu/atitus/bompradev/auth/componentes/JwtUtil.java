package br.edu.atitus.bompradev.auth.componentes;

import br.edu.atitus.bompradev.auth.configs.ConfigJwt;
import br.edu.atitus.bompradev.auth.entidades.Usuario;
import io.jsonwebtoken.Jwts;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey chave;

    public JwtUtil(SecretKey chaveJwt) {
        this.chave = chaveJwt;
    }

    public String gerarToken(Usuario usuario, long duracaoSegundos) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .issuer(ConfigJwt.EMISSOR)
                .subject(usuario.getId().toString())
                .claim("tipo", usuario.getTipo().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(duracaoSegundos)))
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
    }

    public Jwt validarToken(String token) {
        try {
            var tokenAssinado = Jwts.parser().verifyWith(chave)
                    .requireIssuer(ConfigJwt.EMISSOR)
                    .build().parseSignedClaims(token);
            var claims = tokenAssinado.getPayload();
            if (!"HS256".equals(tokenAssinado.getHeader().getAlgorithm())
                    || claims.getSubject() == null || claims.getIssuedAt() == null || claims.getExpiration() == null) {
                throw new BadJwtException("Token inválido");
            }
            return Jwt.withTokenValue(token)
                    .headers(headers -> headers.putAll(tokenAssinado.getHeader()))
                    .claims(valores -> valores.putAll(claims))
                    .issuedAt(claims.getIssuedAt().toInstant())
                    .expiresAt(claims.getExpiration().toInstant())
                    .build();
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new BadJwtException("Token inválido", e);
        }
    }
}
