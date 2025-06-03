package br.com.ederoliv.goeat_api.services;

import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private static final String PINATA_GATEWAY_URL = "https://gateway.pinata.cloud/ipfs";

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
     * Valida se o CID tem formato válido
     */
    public boolean isValidCid(String cid) {
        if (cid == null || cid.trim().isEmpty()) {
            return false;
        }

        String trimmedCid = cid.trim();

        // Validação básica: deve começar com Qm ou baf e ter comprimento mínimo
        return (trimmedCid.startsWith("Qm") && trimmedCid.length() >= 44) ||
                (trimmedCid.startsWith("baf") && trimmedCid.length() >= 56);
    }
}