package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.image.PinataResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pinata.jwt.token}")
    private String pinataJwtToken;

    @Value("${pinata.api.url:https://uploads.pinata.cloud/v3/files}")
    private String pinataApiUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "webp", "gif", "bmp"
    );

    /**
     * Faz upload da imagem para Pinata e retorna apenas o CID
     */
    public String uploadImageAndGetCid(MultipartFile file) throws IOException {
        // Validações
        validateFile(file);
        validatePinataConfiguration();

        try {
            // Preparar requisição para Pinata
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(pinataJwtToken);

            // Preparar FormData
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("network", "public");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Enviando arquivo para Pinata Cloud: {}", file.getOriginalFilename());

            // Fazer requisição para Pinata
            ResponseEntity<String> response = restTemplate.exchange(
                    pinataApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Parse da resposta do Pinata
                PinataResponseDTO pinataResponse = objectMapper.readValue(
                        response.getBody(), PinataResponseDTO.class);

                String cid = pinataResponse.data().cid();

                log.info("Upload concluído com sucesso. CID: {}", cid);

                // Retorna apenas o CID
                return cid;

            } else {
                log.error("Erro na resposta do Pinata. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("Erro na comunicação com Pinata Cloud");
            }

        } catch (Exception e) {
            log.error("Erro ao fazer upload para Pinata Cloud", e);
            throw new RuntimeException("Falha no upload da imagem: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo está vazio");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do arquivo é inválido");
        }

        // Validar extensão
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Tipo de arquivo não permitido. Extensões aceitas: " + ALLOWED_EXTENSIONS);
        }

        // Validar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("O arquivo deve ser uma imagem válida");
        }
    }

    private void validatePinataConfiguration() {
        if (pinataJwtToken == null || pinataJwtToken.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Token JWT do Pinata não configurado. Configure a variável de ambiente 'PINATA_JWT_TOKEN'");
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}