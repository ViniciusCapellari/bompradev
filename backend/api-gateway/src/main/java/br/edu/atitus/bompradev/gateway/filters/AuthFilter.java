package br.edu.atitus.bompradev.gateway.filters;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    private final SecretKey chave;

    public AuthFilter(@Value("${JWT_SECRET}") String segredo) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET deve ter pelo menos 32 bytes");
        chave = new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var limpa = request.mutate().headers(h -> List.of("X-User-Id", "X-User-Type", "X-User-Email").forEach(h::remove)).build();
        String caminho = request.getURI().getPath();
        boolean publica = HttpMethod.OPTIONS.equals(request.getMethod())
                || (HttpMethod.POST.equals(request.getMethod()) && (caminho.equals("/auth/signup") || caminho.equals("/auth/signin")))
                || (HttpMethod.GET.equals(request.getMethod()) && caminho.startsWith("/perfis/"))
                || (HttpMethod.GET.equals(request.getMethod()) && (caminho.equals("/catalogo/categorias")
                    || caminho.equals("/catalogo/servicos") || caminho.startsWith("/catalogo/servicos/")));
        if (publica) return chain.filter(exchange.mutate().request(limpa).build());

        String autorizacao = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        try {
            if (autorizacao == null || !autorizacao.startsWith("Bearer ")) throw new IllegalArgumentException();
            var token = Jwts.parser().verifyWith(chave).requireIssuer("bompradev-auth")
                    .build().parseSignedClaims(autorizacao.substring(7));
            var dados = token.getPayload();
            if (!"HS256".equals(token.getHeader().getAlgorithm()) || dados.getExpiration() == null
                    || dados.getIssuedAt() == null || !List.of("USER", "PROVIDER", "ADMIN").contains(dados.get("tipo", String.class))) {
                throw new IllegalArgumentException();
            }
            UUID.fromString(dados.getSubject());
            return chain.filter(exchange.mutate().request(limpa).build());
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException | NullPointerException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() { return -1; }
}
