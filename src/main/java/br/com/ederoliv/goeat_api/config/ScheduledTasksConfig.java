package br.com.ederoliv.goeat_api.config;

import br.com.ederoliv.goeat_api.entities.Partner;
import br.com.ederoliv.goeat_api.services.OperatingHoursCacheService;
import br.com.ederoliv.goeat_api.services.OperatingHoursService;
import br.com.ederoliv.goeat_api.repositories.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Configuração de tarefas agendadas para atualização automática de status
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTasksConfig {

    private final OperatingHoursService operatingHoursService;
    private final OperatingHoursCacheService cacheService;
    private final PartnerRepository partnerRepository;

    /**
     * Tarefa para atualizar o cache de status de parceiros abertos/fechados
     * Executa a cada 5 minutos (300000ms)
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void updatePartnerOpenStatus() {
        log.info("Iniciando atualização agendada de status de abertura dos parceiros");

        try {
            // Migração: garante que todos os parceiros tenham um valor para isOpen
            List<Partner> partners = partnerRepository.findAll();

            // Conta parceiros atualizados para log
            int updatedCount = 0;

            // Verifica e corrige parceiros com isOpen nulo
            for (Partner partner : partners) {
                // Acessa o campo isOpen através do getter, que retorna o valor default se for nulo
                boolean currentStatus = partner.isOpen();

                // Se o parceiro tiver isOpen nulo, define como true (padrão)
                if (partner.getIsOpen() == null) {
                    partner.setOpen(true); // Usa o setter seguro
                    updatedCount++;
                }
            }

            // Salva os parceiros atualizados, se houver
            if (updatedCount > 0) {
                log.info("Migrando valores nulos de isOpen para {} parceiros", updatedCount);
                partnerRepository.saveAll(partners);
            }

            log.info("Total de parceiros verificados: {}", partners.size());
            log.info("Atualização de status concluída com sucesso");
        } catch (Exception e) {
            log.error("Erro ao atualizar status de parceiros", e);
        }
    }

    /**
     * Tarefa para limpar o cache de status de parceiros abertos/fechados
     * Executa a cada 5 minutos (300000ms)
     */
    @Scheduled(fixedRate = 300000)
    public void clearOpenStatusCache() {
        log.info("Iniciando limpeza do cache de status de abertura dos parceiros");

        try {
            // Limpar todo o cache para forçar uma nova verificação
            cacheService.invalidateAllCache();

            log.info("Cache de status de abertura limpo com sucesso");
        } catch (Exception e) {
            log.error("Erro ao limpar cache de status", e);
        }
    }
}