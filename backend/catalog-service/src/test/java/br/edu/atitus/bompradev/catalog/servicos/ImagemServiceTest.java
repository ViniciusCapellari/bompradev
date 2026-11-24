package br.edu.atitus.bompradev.catalog.servicos;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImagemServiceTest {
    private final ImagemService imagens = new ImagemService("");

    @Test
    void rejeitaArquivoQueNaoEImagemMesmoComTipoDeclarado() {
        var arquivo = new MockMultipartFile("imagem", "imagem.png", "image/png", new byte[]{1, 2, 3});
        var erro = assertThrows(ResponseStatusException.class, () -> imagens.enviar(arquivo));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }

    @Test
    void informaQuandoNaoHaConfiguracaoParaEnvio() throws Exception {
        var conteudo = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", conteudo);
        var arquivo = new MockMultipartFile("imagem", "imagem.png", "image/png", conteudo.toByteArray());
        var erro = assertThrows(ResponseStatusException.class, () -> imagens.enviar(arquivo));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, erro.getStatusCode());
    }
}
