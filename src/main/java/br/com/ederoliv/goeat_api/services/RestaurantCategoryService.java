package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.restaurantCategory.PartnerCategoryUpdateDTO;
import br.com.ederoliv.goeat_api.dto.restaurantCategory.RestaurantCategoryResponseDTO;
import br.com.ederoliv.goeat_api.entities.Partner;
import br.com.ederoliv.goeat_api.entities.RestaurantCategory;
import br.com.ederoliv.goeat_api.repositories.PartnerRepository;
import br.com.ederoliv.goeat_api.repositories.RestaurantCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantCategoryService {

    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final PartnerRepository partnerRepository;
    private final AuthenticationUtilsService authenticationUtilsService;

    /**
     * Lista todas as categorias de restaurante disponíveis
     */
    public List<RestaurantCategoryResponseDTO> getAllCategories() {
        return restaurantCategoryRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista as categorias de um parceiro específico
     */
    public List<RestaurantCategoryResponseDTO> getPartnerCategories(Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        return restaurantCategoryRepository.findByPartnerId(partnerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista as categorias disponíveis para um parceiro (que ele ainda não possui)
     */
    public List<RestaurantCategoryResponseDTO> getAvailableCategoriesForPartner(Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        return restaurantCategoryRepository.findAvailableForPartner(partnerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza as categorias de um parceiro
     */
    @Transactional
    public List<RestaurantCategoryResponseDTO> updatePartnerCategories(
            PartnerCategoryUpdateDTO updateDTO,
            Authentication authentication) {

        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        // Buscar as categorias pelos IDs fornecidos
        List<RestaurantCategory> newCategories = new ArrayList<>();
        if (updateDTO.categoryIds() != null && !updateDTO.categoryIds().isEmpty()) {
            newCategories = restaurantCategoryRepository.findAllById(updateDTO.categoryIds());

            // Verificar se todas as categorias foram encontradas
            if (newCategories.size() != updateDTO.categoryIds().size()) {
                throw new EntityNotFoundException("Uma ou mais categorias não foram encontradas");
            }
        }

        // Atualizar as categorias do parceiro
        partner.setRestaurantCategories(newCategories);
        Partner updatedPartner = partnerRepository.save(partner);

        log.info("Categorias do parceiro {} atualizadas com sucesso. Total: {}",
                partnerId, newCategories.size());

        // Retornar as categorias atualizadas
        return updatedPartner.getRestaurantCategories()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Adiciona uma categoria a um parceiro
     */
    @Transactional
    public List<RestaurantCategoryResponseDTO> addCategoryToPartner(
            Long categoryId,
            Authentication authentication) {

        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        RestaurantCategory category = restaurantCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        // Verificar se o parceiro já possui esta categoria
        if (partner.getRestaurantCategories() == null) {
            partner.setRestaurantCategories(new ArrayList<>());
        }

        if (partner.getRestaurantCategories().contains(category)) {
            throw new IllegalStateException("Parceiro já possui esta categoria");
        }

        // Adicionar a categoria
        partner.getRestaurantCategories().add(category);
        Partner updatedPartner = partnerRepository.save(partner);

        log.info("Categoria {} adicionada ao parceiro {} com sucesso",
                category.getName(), partnerId);

        return updatedPartner.getRestaurantCategories()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Remove uma categoria de um parceiro
     */
    @Transactional
    public List<RestaurantCategoryResponseDTO> removeCategoryFromPartner(
            Long categoryId,
            Authentication authentication) {

        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        RestaurantCategory category = restaurantCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        // Verificar se o parceiro possui esta categoria
        if (partner.getRestaurantCategories() == null ||
                !partner.getRestaurantCategories().contains(category)) {
            throw new IllegalStateException("Parceiro não possui esta categoria");
        }

        // Remover a categoria
        partner.getRestaurantCategories().remove(category);
        Partner updatedPartner = partnerRepository.save(partner);

        log.info("Categoria {} removida do parceiro {} com sucesso",
                category.getName(), partnerId);

        return updatedPartner.getRestaurantCategories()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mapeia RestaurantCategory para DTO de resposta
     */
    private RestaurantCategoryResponseDTO mapToResponseDTO(RestaurantCategory category) {
        return new RestaurantCategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}