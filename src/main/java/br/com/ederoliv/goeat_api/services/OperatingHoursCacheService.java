package br.com.ederoliv.goeat_api.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço para armazenar em cache o status de abertura dos restaurantes
 * Isso reduz o número de consultas ao banco de dados para verificar
 * se um restaurante está aberto.
 */
@Slf4j
@Service
public class OperatingHoursCacheService {

    // Cache para armazenar o status de abertura por parceiro
    private final Map<UUID, CachedOpenStatus> openStatusCache = new ConcurrentHashMap<>();

    // Tempo de expiração do cache em milissegundos (5 minutos)
    private static final long CACHE_EXPIRATION_MS = 5 * 60 * 1000;

    /**
     * Obtém o status de abertura do cache
     * @param partnerId ID do parceiro
     * @return Status de abertura ou null se não estiver em cache ou expirado
     */
    public Boolean getOpenStatus(UUID partnerId) {
        CachedOpenStatus cachedStatus = openStatusCache.get(partnerId);

        if (cachedStatus == null) {
            return null; // Não está em cache
        }

        // Verificar se o cache expirou
        if (isExpired(cachedStatus.timestamp)) {
            openStatusCache.remove(partnerId);
            return null;
        }

        return cachedStatus.isOpen;
    }

    /**
     * Armazena o status de abertura no cache
     * @param partnerId ID do parceiro
     * @param isOpen Status de abertura
     */
    public void setOpenStatus(UUID partnerId, boolean isOpen) {
        openStatusCache.put(partnerId, new CachedOpenStatus(isOpen, Instant.now().toEpochMilli()));
    }

    /**
     * Limpa o cache de um parceiro específico
     * @param partnerId ID do parceiro
     */
    public void invalidateCache(UUID partnerId) {
        openStatusCache.remove(partnerId);
    }

    /**
     * Limpa todo o cache
     */
    public void invalidateAllCache() {
        openStatusCache.clear();
    }

    /**
     * Verifica se um timestamp expirou
     * @param timestamp Timestamp em milissegundos
     * @return true se expirou, false caso contrário
     */
    private boolean isExpired(long timestamp) {
        return Instant.now().toEpochMilli() - timestamp > CACHE_EXPIRATION_MS;
    }

    /**
     * Classe interna para armazenar o status de abertura com timestamp
     */
    private static class CachedOpenStatus {
        final boolean isOpen;
        final long timestamp;

        CachedOpenStatus(boolean isOpen, long timestamp) {
            this.isOpen = isOpen;
            this.timestamp = timestamp;
        }
    }
}