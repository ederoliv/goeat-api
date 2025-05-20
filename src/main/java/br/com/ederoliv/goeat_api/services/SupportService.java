package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.support.SupportMessageResponseDTO;
import br.com.ederoliv.goeat_api.dto.support.SupportRequestDTO;
import br.com.ederoliv.goeat_api.dto.support.SupportResponseDTO;
import br.com.ederoliv.goeat_api.entities.Partner;
import br.com.ederoliv.goeat_api.entities.Support;
import br.com.ederoliv.goeat_api.entities.SupportMessage;
import br.com.ederoliv.goeat_api.entities.SupportStatus;
import br.com.ederoliv.goeat_api.repositories.PartnerRepository;
import br.com.ederoliv.goeat_api.repositories.SupportMessageRepository;
import br.com.ederoliv.goeat_api.repositories.SupportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportRepository supportRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final PartnerRepository partnerRepository;
    private final AuthenticationUtilsService authenticationUtilsService;

    /**
     * Cria um novo chamado para um parceiro
     */
    @Transactional
    public SupportResponseDTO createSupport(SupportRequestDTO requestDTO, Authentication authentication) {
        // Extrai o partnerId do token JWT
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        Support support = new Support();
        support.setTitle(requestDTO.title());
        support.setDescription(requestDTO.description());
        support.setPartner(partner);
        support.setStatus(SupportStatus.ABERTO);
        support.setCreatedAt(LocalDateTime.now());
        support.setUpdatedAt(LocalDateTime.now());

        Support savedSupport = supportRepository.save(support);

        // Adiciona a descrição como a primeira mensagem do parceiro
        SupportMessage firstMessage = new SupportMessage();
        firstMessage.setContent(requestDTO.description());
        firstMessage.setFromSupport(false); // mensagem do parceiro
        firstMessage.setSupport(savedSupport);
        firstMessage.setCreatedAt(LocalDateTime.now());
        supportMessageRepository.save(firstMessage);

        return mapToResponseDTO(savedSupport);
    }

    /**
     * Lista todos os chamados de um parceiro
     */
    public List<SupportResponseDTO> getAllSupportsByPartnerId(Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        return supportRepository.findSupportByPartnerId(partnerId)
                .orElse(List.of())
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista todas as mensagens de um chamado
     */
    public List<SupportMessageResponseDTO> getAllMessagesBySupportId(UUID supportId, Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        // Verifica se o chamado existe e pertence ao parceiro autenticado
        Support support = supportRepository.findById(supportId)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));

        if (!support.getPartner().getId().equals(partnerId)) {
            throw new SecurityException("Você não tem permissão para acessar este chamado");
        }

        // Busca as mensagens ordenadas por data de criação (mais antigas primeiro)
        return supportMessageRepository.findBySupportIdOrderByCreatedAtAsc(supportId)
                .stream()
                .map(message -> new SupportMessageResponseDTO(
                        message.getId(),
                        message.getContent(),
                        message.isFromSupport(),
                        message.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Adiciona uma nova mensagem a um chamado existente
     */
    @Transactional
    public SupportMessageResponseDTO addMessageToSupport(UUID supportId, String content, Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        // Verifica se o chamado existe e pertence ao parceiro autenticado
        Support support = supportRepository.findById(supportId)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));

        if (!support.getPartner().getId().equals(partnerId)) {
            throw new SecurityException("Você não tem permissão para acessar este chamado");
        }

        // Adiciona a nova mensagem
        SupportMessage message = new SupportMessage();
        message.setContent(content);
        message.setFromSupport(false); // mensagem do parceiro
        message.setSupport(support);
        message.setCreatedAt(LocalDateTime.now());

        SupportMessage savedMessage = supportMessageRepository.save(message);

        // Atualiza a data de última atualização do chamado
        support.setUpdatedAt(LocalDateTime.now());
        supportRepository.save(support);

        return new SupportMessageResponseDTO(
                savedMessage.getId(),
                savedMessage.getContent(),
                savedMessage.isFromSupport(),
                savedMessage.getCreatedAt()
        );
    }

    /**
     * Mapeia uma entidade Support para SupportResponseDTO
     */
    private SupportResponseDTO mapToResponseDTO(Support support) {
        return new SupportResponseDTO(
                support.getId(),
                support.getTitle(),
                support.getDescription(),
                support.getStatus(),
                support.getCreatedAt(),
                support.getUpdatedAt(),
                support.getResolvedAt(),
                support.getPartner().getId()
        );
    }
}