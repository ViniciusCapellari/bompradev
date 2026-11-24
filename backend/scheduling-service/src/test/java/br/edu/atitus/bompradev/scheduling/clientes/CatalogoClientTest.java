package br.edu.atitus.bompradev.scheduling.clientes;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoClientTest {
    private HttpServer servidor;
    private CatalogoClient catalogo;
    private final AtomicInteger chamadas = new AtomicInteger();
    private final UUID servicoId = UUID.randomUUID();
    private int status;

    @BeforeEach
    void iniciar() throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/catalogo/servicos/", troca -> {
            chamadas.incrementAndGet();
            byte[] resposta = "{}".getBytes(StandardCharsets.UTF_8);
            troca.sendResponseHeaders(status, resposta.length);
            troca.getResponseBody().write(resposta);
            troca.close();
        });
        servidor.start();
        catalogo = new CatalogoClient(WebClient.builder(), "http://127.0.0.1:" + servidor.getAddress().getPort());
    }

    @AfterEach
    void encerrar() { servidor.stop(0); }

    @Test
    void naoRepeteServicoInexistente() {
        status = 404;
        var erro = assertThrows(ResponseStatusException.class, () -> catalogo.consultar(servicoId));
        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        assertEquals(1, chamadas.get());
    }

    @Test
    void repeteFalhaTemporariaSemCriarAgendamento() {
        status = 503;
        var erro = assertThrows(ResponseStatusException.class, () -> catalogo.consultar(servicoId));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, erro.getStatusCode());
        assertEquals(3, chamadas.get());
    }

    @Test
    void abreCircuitoDepoisDeFalhasRepetidas() {
        status = 503;
        for (int i = 0; i < 5; i++) assertThrows(ResponseStatusException.class, () -> catalogo.consultar(servicoId));
        assertEquals(15, chamadas.get());
        var erro = assertThrows(ResponseStatusException.class, () -> catalogo.consultar(servicoId));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, erro.getStatusCode());
        assertEquals(15, chamadas.get());
    }
}
