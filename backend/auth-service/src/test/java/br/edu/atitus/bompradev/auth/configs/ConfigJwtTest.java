package br.edu.atitus.bompradev.auth.configs;

import br.edu.atitus.bompradev.auth.componentes.JwtUtil;
import br.edu.atitus.bompradev.auth.entidades.TipoUsuario;
import br.edu.atitus.bompradev.auth.entidades.Usuario;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Date;
import java.util.Base64;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;

import static org.junit.jupiter.api.Assertions.*;

class ConfigJwtTest {
    @Test
    void aceitaTokenNoFormatoEmitidoAnteriormente() throws Exception {
        var configuracao = new ConfigJwt();
        var chave = configuracao.chaveJwt("chave-para-testes-com-mais-de-32-bytes");
        var base64 = Base64.getUrlEncoder().withoutPadding();
        long agora = Instant.now().getEpochSecond();
        String cabecalho = base64.encodeToString("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8));
        String dados = "{\"iss\":\"bompradev-auth\",\"sub\":\"usuario\",\"iat\":" + agora
                + ",\"exp\":" + (agora + 3600) + ",\"tipo\":\"USER\"}";
        String conteudo = cabecalho + "." + base64.encodeToString(dados.getBytes(StandardCharsets.UTF_8));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(chave);
        String token = conteudo + "." + base64.encodeToString(mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8)));

        assertEquals("usuario", new JwtUtil(chave).validarToken(token).getSubject());
    }

    @Test
    void validaAssinaturaEmissorEExpiracao() {
        var configuracao = new ConfigJwt();
        var chave = configuracao.chaveJwt("chave-para-testes-com-mais-de-32-bytes");
        var util = new JwtUtil(chave);
        var decoder = configuracao.jwtDecoder(util);
        var usuario = new Usuario("Pessoa", "pessoa@exemplo.com", "hash", TipoUsuario.USER);
        var id = UUID.randomUUID();
        ReflectionTestUtils.setField(usuario, "id", id);
        var token = util.gerarToken(usuario, 3600);

        assertEquals(id.toString(), decoder.decode(token).getSubject());
        var outraChave = configuracao.chaveJwt("outra-chave-para-testes-com-mais-de-32-bytes");
        assertThrows(JwtException.class, () -> new JwtUtil(outraChave).validarToken(token));
        var agora = Instant.now();
        var tokenExpirado = Jwts.builder().issuer(ConfigJwt.EMISSOR).subject(id.toString())
                .issuedAt(Date.from(agora.minusSeconds(180))).expiration(Date.from(agora.minusSeconds(120)))
                .signWith(chave, Jwts.SIG.HS256).compact();
        assertThrows(JwtException.class, () -> decoder.decode(tokenExpirado));
        var tokenEmissorInvalido = Jwts.builder().issuer("emissor-externo").subject(id.toString())
                .issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600)))
                .signWith(chave, Jwts.SIG.HS256).compact();
        assertThrows(JwtException.class, () -> decoder.decode(tokenEmissorInvalido));
        var chaveMaisLonga = configuracao.chaveJwt("chave-de-teste-para-validacao-de-algoritmo-mais-longa-que-48-bytes");
        var algoritmoDiferente = Jwts.builder().issuer(ConfigJwt.EMISSOR).subject(id.toString())
                .issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600)))
                .signWith(chaveMaisLonga, Jwts.SIG.HS384).compact();
        assertThrows(JwtException.class, () -> new JwtUtil(chaveMaisLonga).validarToken(algoritmoDiferente));
        var semExpiracao = Jwts.builder().issuer(ConfigJwt.EMISSOR).subject(id.toString())
                .issuedAt(Date.from(agora)).signWith(chave, Jwts.SIG.HS256).compact();
        assertThrows(JwtException.class, () -> decoder.decode(semExpiracao));
        assertThrows(IllegalArgumentException.class, () -> configuracao.chaveJwt("curta"));
    }
}
