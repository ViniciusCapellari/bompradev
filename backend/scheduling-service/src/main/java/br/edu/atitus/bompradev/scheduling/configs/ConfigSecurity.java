package br.edu.atitus.bompradev.scheduling.configs;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Configuration
public class ConfigSecurity {
    @Bean
    JwtDecoder jwtDecoder(@Value("${JWT_SECRET}") String segredo) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET deve ter pelo menos 32 bytes");
        var decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(bytes, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> camposObrigatorios = token -> {
            try {
                UUID.fromString(token.getSubject());
                if (token.getIssuedAt() == null || token.getExpiresAt() == null
                        || !List.of("USER", "PROVIDER", "ADMIN").contains(token.getClaimAsString("tipo"))) {
                    throw new IllegalArgumentException();
                }
                return OAuth2TokenValidatorResult.success();
            } catch (IllegalArgumentException | NullPointerException e) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
            }
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer("bompradev-auth"), camposObrigatorios));
        return decoder;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/agenda/disponibilidades", "/agenda/disponibilidades/*").hasAuthority("PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/agenda/horarios", "/agenda/agendamentos/meus").hasAnyAuthority("USER", "PROVIDER")
                        .requestMatchers(HttpMethod.POST, "/agenda/agendamentos").hasAnyAuthority("USER", "PROVIDER")
                        .requestMatchers(HttpMethod.PUT, "/agenda/agendamentos/*/pagamento").hasAnyAuthority("USER", "PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/agenda/agendamentos/recebidos").hasAuthority("PROVIDER")
                        .requestMatchers(HttpMethod.PUT, "/agenda/agendamentos/*/aprovar", "/agenda/agendamentos/*/recusar").hasAuthority("PROVIDER")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(this::autenticacao)))
                .build();
    }

    private AbstractAuthenticationToken autenticacao(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(jwt.getClaimAsString("tipo"))));
    }
}
