package br.com.ederoliv.goeat_api.services;

import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private static final String PINATA_GATEWAY_URL = "https://gateway.ipfs.io/ipfs";
    //https://gateway.pinata.cloud/ipfs

    /**
     * Constrói URL completa a partir do CID para exibição no frontend
     * Recebe: "bafkreieii6mm36mxhpxt5rvwhq3cdyhhwclzdekh6ppccxegrtu46kxd5a"
     * Retorna: "https://gateway.pinata.cloud/ipfs/bafkreieii6mm36mxhpxt5rvwhq3cdyhhwclzdekh6ppccxegrtu46kxd5a"
     */
    public String buildImageUrl(String cid) {
        if (cid == null || cid.trim().isEmpty()) {
            return null;
        }

        String trimmedCid = cid.trim();

        // Se já for uma URL completa, retorna como está
        if (trimmedCid.startsWith("http")) {
            return trimmedCid;
        }

        // Constrói a URL completa
        return PINATA_GATEWAY_URL + "/" + trimmedCid;
    }

    /**
     * Constrói URL para imagem do produto
     * Se o CID for null/vazio, retorna null (sem imagem)
     */
    public String buildProductImageUrl(String cid) {
        return buildImageUrl(cid);
    }

    /**
     * Constrói URL para imagem do parceiro (restaurante)
     * Se o CID for null/vazio, retorna null (sem imagem)
     */
    public String buildPartnerImageUrl(String cid) {
        return buildImageUrl(cid);
    }

    /**
     * Constrói URL para imagem do cliente
     * Se o CID for null/vazio, retorna null (sem imagem)
     */
    public String buildClientImageUrl(String cid) {
        return buildImageUrl(cid);
    }

    /**
     * Extrai o CID de uma URL completa ou retorna o valor original se já for um CID
     * Recebe: "https://gateway.pinata.cloud/ipfs/bafkreieii6mm36mxhpxt5rvwhq3cdyhhwclzdekh6ppccxegrtu46kxd5a"
     * Retorna: "bafkreieii6mm36mxhpxt5rvwhq3cdyhhwclzdekh6ppccxegrtu46kxd5a"
     */
    public String extractCidFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        String trimmedUrl = imageUrl.trim();

        // Se já for um CID válido, retorna como está
        if (isValidCid(trimmedUrl)) {
            return trimmedUrl;
        }

        // Se for uma URL completa, extrai o CID
        if (trimmedUrl.startsWith("http")) {
            int lastSlashIndex = trimmedUrl.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < trimmedUrl.length() - 1) {
                String extractedCid = trimmedUrl.substring(lastSlashIndex + 1);
                // Valida o CID extraído
                if (isValidCid(extractedCid)) {
                    return extractedCid;
                }
            }
        }

        return trimmedUrl;
    }

    /**
     * Valida se o CID tem formato válido
     * Suporta tanto CIDs v0 (Qm...) quanto v1 (baf...)
     */
    public boolean isValidCid(String cid) {
        if (cid == null || cid.trim().isEmpty()) {
            return false;
        }

        String trimmedCid = cid.trim();

        // Validação básica: deve começar com Qm ou baf e ter comprimento mínimo
        return (trimmedCid.startsWith("Qm") && trimmedCid.length() >= 44) ||
                (trimmedCid.startsWith("baf") && trimmedCid.length() >= 56) ||
                (trimmedCid.startsWith("bafy") && trimmedCid.length() >= 56);
    }

    /**
     * Processa uma string que pode ser um CID ou URL completa
     * Sempre retorna um CID válido ou null
     */
    public String processToCid(String imageInput) {
        if (imageInput == null || imageInput.trim().isEmpty()) {
            return null;
        }

        String extracted = extractCidFromUrl(imageInput);
        return isValidCid(extracted) ? extracted : null;
    }

    /**
     * Processa uma string que pode ser um CID ou URL parcial
     * Sempre retorna uma URL completa ou null
     */
    public String processToUrl(String imageInput) {
        String cid = processToCid(imageInput);
        return buildImageUrl(cid);
    }

    /**
     * Verifica se uma string é uma URL completa
     */
    public boolean isCompleteUrl(String url) {
        return url != null && url.trim().startsWith("http");
    }

    /**
     * Verifica se uma string é apenas um CID
     */
    public boolean isCidOnly(String input) {
        return input != null && !isCompleteUrl(input) && isValidCid(input);
    }
}