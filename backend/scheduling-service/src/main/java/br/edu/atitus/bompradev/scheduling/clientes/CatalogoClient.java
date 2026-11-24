package br.edu.atitus.bompradev.scheduling.clientes;

import br.edu.atitus.bompradev.scheduling.dtos.ServicoCatalogo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class CatalogoClient {
    private final WebClient cliente;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public CatalogoClient(WebClient.Builder builder, @Value("${CATALOG_SERVICE_URL:http://catalog-service}") String url) {
        this.cliente = builder.baseUrl(url).build();
        this.retry = Retry.of("catalogo", RetryConfig.custom().maxAttempts(3)
                .waitDuration(Duration.ofMillis(200)).retryOnException(CatalogoClient::falhaTemporaria).build());
        this.circuitBreaker = CircuitBreaker.of("catalogo", CircuitBreakerConfig.custom()
                .slidingWindowSize(10).minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .recordException(CatalogoClient::falhaTemporaria).build());
    }

    public ServicoCatalogo consultar(UUID servicoId) {
        try {
            Supplier<ServicoCatalogo> chamada = () -> cliente.get().uri("/catalogo/servicos/{id}", servicoId).retrieve()
                    .bodyToMono(ServicoCatalogo.class).block(Duration.ofSeconds(5));
            var servico = CircuitBreaker.decorateSupplier(circuitBreaker, Retry.decorateSupplier(retry, chamada)).get();
            if (servico == null || !servicoId.equals(servico.id()) || servico.providerId() == null
                    || servico.duracaoMinutos() == null || servico.duracaoMinutos() <= 0
                    || servico.valor() == null || servico.valor().signum() < 0) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Resposta inválida do catálogo");
            }
            return servico;
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catálogo indisponível", e);
        }
    }

    private static boolean falhaTemporaria(Throwable erro) {
        if (erro instanceof WebClientResponseException resposta) return resposta.getStatusCode().is5xxServerError();
        return !(erro instanceof ResponseStatusException);
    }
}
