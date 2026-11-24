package br.edu.atitus.bompradev.catalog.servicos;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class ImagemService {
    private static final long LIMITE_BYTES = 5 * 1024 * 1024;
    private final String configuracao;

    public ImagemService(@Value("${CLOUDINARY_URL:}") String configuracao) {
        this.configuracao = configuracao;
    }

    public String enviar(MultipartFile imagem) {
        if (imagem.isEmpty() || imagem.getSize() > LIMITE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A imagem deve ter até 5 MB");
        }
        try {
            byte[] conteudo = imagem.getBytes();
            try (var entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(conteudo))) {
                var leitores = ImageIO.getImageReaders(entrada);
                if (!leitores.hasNext()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie uma imagem JPEG ou PNG válida");
                }
                var leitor = leitores.next();
                try {
                    var formato = leitor.getFormatName();
                    if (!formato.equalsIgnoreCase("jpeg") && !formato.equalsIgnoreCase("png")) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie uma imagem JPEG ou PNG válida");
                    }
                    leitor.setInput(entrada, true, true);
                    if (leitor.getWidth(0) > 10000 || leitor.getHeight(0) > 10000) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dimensões da imagem excedem o limite");
                    }
                } finally {
                    leitor.dispose();
                }
            }
            if (configuracao.isBlank()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Envio de imagens não configurado");
            }
            var resultado = new Cloudinary(configuracao).uploader().upload(conteudo, Map.of("resource_type", "image"));
            var url = resultado.get("secure_url");
            if (!(url instanceof String endereco) || !endereco.startsWith("https://res.cloudinary.com/") || endereco.length() > 500) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Resposta inválida do serviço de imagens");
            }
            return endereco;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível enviar a imagem");
        }
    }
}
