package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.image.ImageUploadResponseDTO;
import br.com.ederoliv.goeat_api.services.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/upload")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validação básica do arquivo
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Nenhum arquivo foi enviado");
            }

            // Validar se é uma imagem
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("O arquivo deve ser uma imagem válida");
            }

            // Validar tamanho (max 5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB em bytes
            if (file.getSize() > maxSize) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("O arquivo não pode exceder 5MB");
            }

            log.info("Iniciando upload de imagem: {} - Tamanho: {} bytes - Tipo: {}",
                    file.getOriginalFilename(), file.getSize(), contentType);

            // Upload para Pinata e retorna apenas o CID
            String cid = imageUploadService.uploadImageAndGetCid(file);

            log.info("Upload concluído com sucesso. CID: {}", cid);

            // Retorna apenas o CID (resposta otimizada)
            return ResponseEntity.ok(new ImageUploadResponseDTO(cid));

        } catch (IllegalArgumentException e) {
            log.warn("Erro de validação no upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro interno no upload de imagem", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
}