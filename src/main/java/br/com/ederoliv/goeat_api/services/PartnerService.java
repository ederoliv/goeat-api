package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.address.AddressRequestDTO;
import br.com.ederoliv.goeat_api.dto.partner.*;
import br.com.ederoliv.goeat_api.dto.restaurantCategory.RestaurantCategoryResponseDTO;
import br.com.ederoliv.goeat_api.entities.*;
import br.com.ederoliv.goeat_api.repositories.MenuRepository;
import br.com.ederoliv.goeat_api.repositories.PartnerRepository;
import br.com.ederoliv.goeat_api.repositories.RestaurantCategoryRepository;
import br.com.ederoliv.goeat_api.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationUtilsService authenticationUtilsService;
    private final RestaurantCategoryRepository restaurantCategoryRepository;

    public PartnerResponseDTO getPartnerById(UUID partnerId) {
        Optional<Partner> partner = partnerRepository.findById(partnerId);
        return partner.map(p -> new PartnerResponseDTO(p.getId(), p.getName())).orElse(null);
    }

    public List<PartnerResponseDTO> listAllPartners() {
        List<Partner> partners = partnerRepository.findAll();
        return partners.stream()
                .map(partner -> new PartnerResponseDTO(partner.getId(), partner.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os parceiros com suas categorias
     */
    public List<PartnerWithCategoriesResponseDTO> listAllPartnersWithCategories() {
        List<Partner> partners = partnerRepository.findAll();
        return partners.stream()
                .map(this::mapToPartnerWithCategoriesDTO)
                .collect(Collectors.toList());
    }

    public Optional<Partner> findByEmail(String email) {
        Optional<User> user = userRepository.findByUsername(email);
        if (user.isPresent() && user.get().getPartner() != null) {
            return Optional.of(user.get().getPartner());
        }
        return Optional.empty();
    }

    public Optional<Partner> findById(UUID id) {
        return partnerRepository.findById(id);
    }

    @Transactional
    public PartnerResponseDTO registerPartner(PartnerRequestDTO request) {
        // Validar se já existe usuário com este email
        if (userRepository.existsByUsername(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        // Validar se já existe parceiro com este CNPJ
        if (partnerRepository.existsByCnpj(request.cnpj())) {
            throw new IllegalArgumentException("CNPJ já cadastrado");
        }

        // Criar usuário
        User user = new User();
        user.setUsername(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("ROLE_PARTNER");

        // Salvar usuário
        User savedUser = userRepository.save(user);

        // Criar parceiro
        Partner partner = new Partner();
        partner.setName(request.name());
        partner.setCnpj(request.cnpj());
        partner.setPhone(request.phone());
        partner.setUser(savedUser);

        // Inicializar lista de categorias como vazia (será adicionada posteriormente pelo dashboard)
        partner.setRestaurantCategories(new ArrayList<>());

        // Salvar parceiro
        Partner savedPartner = partnerRepository.save(partner);

        // Criar menu para o parceiro
        Menu menu = new Menu();
        menu.setPartner(savedPartner);
        menu.setDescription("Menu principal");
        menuRepository.save(menu);

        return new PartnerResponseDTO(savedPartner.getId(), savedPartner.getName());
    }

    /**
     * Lista parceiros por categoria de restaurante (ID)
     */
    public List<PartnerResponseDTO> getPartnersByCategory(Long categoryId) {
        List<Partner> partners = partnerRepository.findPartnersByCategoryId(categoryId);
        return partners.stream()
                .map(partner -> new PartnerResponseDTO(partner.getId(), partner.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Lista parceiros por nome da categoria de restaurante
     */
    public List<PartnerResponseDTO> getPartnersByCategoryName(String categoryName) {
        List<Partner> partners = partnerRepository.findPartnersByCategoryName(categoryName);
        return partners.stream()
                .map(partner -> new PartnerResponseDTO(partner.getId(), partner.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Lista parceiros por categoria com suas categorias incluídas
     */
    public List<PartnerWithCategoriesResponseDTO> getPartnersWithCategoriesByCategory(Long categoryId) {
        List<Partner> partners = partnerRepository.findPartnersByCategoryId(categoryId);
        return partners.stream()
                .map(this::mapToPartnerWithCategoriesDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista parceiros que possuem categorias
     */
    public List<PartnerWithCategoriesResponseDTO> getPartnersWithCategories() {
        List<Partner> partners = partnerRepository.findPartnersWithCategories();
        return partners.stream()
                .map(this::mapToPartnerWithCategoriesDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mapeia Partner para PartnerWithCategoriesResponseDTO
     */
    private PartnerWithCategoriesResponseDTO mapToPartnerWithCategoriesDTO(Partner partner) {
        List<RestaurantCategoryResponseDTO> categories = partner.getRestaurantCategories() != null
                ? partner.getRestaurantCategories().stream()
                .map(category -> new RestaurantCategoryResponseDTO(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new PartnerWithCategoriesResponseDTO(
                partner.getId(),
                partner.getName(),
                partner.getPhone(),
                categories
        );
    }

    /**
     * Atualiza os dados do parceiro incluindo informações básicas, endereço e categorias
     */
    @Transactional
    public PartnerResponseDTO updatePartnerDetails(PartnerDetailsData detailsData, Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        // Buscar o parceiro
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        User user = partner.getUser();
        if (user == null) {
            throw new EntityNotFoundException("Dados de usuário não encontrados");
        }

        // 1. Atualizar informações básicas
        if (detailsData.name() != null && !detailsData.name().isBlank()) {
            partner.setName(detailsData.name());
        }

        if (detailsData.phone() != null) {
            partner.setPhone(detailsData.phone());
        }

        if (detailsData.email() != null && !detailsData.email().isBlank() &&
                !detailsData.email().equals(user.getUsername())) {
            // Verificar se o email já está em uso por outro usuário
            if (userRepository.existsByUsername(detailsData.email())) {
                throw new IllegalArgumentException("Email já está sendo utilizado");
            }

            // Atualizar o email (username)
            user.setUsername(detailsData.email());
            userRepository.save(user);
        }

        // 2. Atualizar endereço se fornecido
        if (detailsData.address() != null) {
            updatePartnerAddress(partner, detailsData.address());
        }

        // 3. Atualizar categorias se fornecidas
        if (detailsData.categoryIds() != null) {
            updatePartnerCategories(partner, detailsData.categoryIds());
        }

        // Salvar o parceiro atualizado
        Partner updatedPartner = partnerRepository.save(partner);

        return new PartnerResponseDTO(updatedPartner.getId(), updatedPartner.getName());
    }

    /**
     * Atualiza o endereço do parceiro
     */
    private void updatePartnerAddress(Partner partner, AddressRequestDTO addressDTO) {
        // Verificar se o parceiro já tem um endereço
        if (partner.getAddress() != null) {
            // Atualizar o endereço existente
            Address address = partner.getAddress();
            address.setStreet(addressDTO.street());
            address.setNumber(addressDTO.number());
            address.setComplement(addressDTO.complement());
            address.setNeighborhood(addressDTO.neighborhood());
            address.setCity(addressDTO.city());
            address.setState(addressDTO.state());
            address.setZipCode(addressDTO.zipCode());
            address.setReference(addressDTO.reference());
        } else {
            // Criar um novo endereço
            Address address = new Address();
            address.setStreet(addressDTO.street());
            address.setNumber(addressDTO.number());
            address.setComplement(addressDTO.complement());
            address.setNeighborhood(addressDTO.neighborhood());
            address.setCity(addressDTO.city());
            address.setState(addressDTO.state());
            address.setZipCode(addressDTO.zipCode());
            address.setReference(addressDTO.reference());
            address.setPartner(partner);
            address.setClient(null);

            partner.setAddress(address);
        }
    }

    /**
     * Atualiza as categorias do parceiro
     */
    private void updatePartnerCategories(Partner partner, List<Long> categoryIds) {
        // Buscar as categorias pelos IDs fornecidos
        List<RestaurantCategory> categories = restaurantCategoryRepository.findAllById(categoryIds);

        // Verificar se todas as categorias foram encontradas
        if (categories.size() != categoryIds.size()) {
            throw new EntityNotFoundException("Uma ou mais categorias não foram encontradas");
        }

        // Atualizar as categorias do parceiro
        partner.setRestaurantCategories(categories);
    }

    // Adicionar ao PartnerService.java

    /**
     * Obtém os dados do perfil do parceiro incluindo informações básicas, endereço e categorias
     */
    public PartnerDetailsResponseDTO getPartnerProfile(Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        // Buscar o parceiro com todos os dados necessários
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        // Formatar o endereço como string formatada
        String formattedAddress = "";
        if (partner.getAddress() != null) {
            Address address = partner.getAddress();
            StringBuilder sb = new StringBuilder();

            sb.append(address.getStreet());

            if (address.getNumber() != null && !address.getNumber().isEmpty()) {
                sb.append(", ").append(address.getNumber());
            }

            if (address.getComplement() != null && !address.getComplement().isEmpty()) {
                sb.append(", ").append(address.getComplement());
            }

            if (address.getNeighborhood() != null && !address.getNeighborhood().isEmpty()) {
                sb.append(", ").append(address.getNeighborhood());
            }

            if (address.getCity() != null && !address.getCity().isEmpty()) {
                sb.append(", ").append(address.getCity());

                if (address.getState() != null && !address.getState().isEmpty()) {
                    sb.append(" - ").append(address.getState());
                }
            }

            formattedAddress = sb.toString();
        }

        // Mapear as categorias para DTOs
        List<RestaurantCategoryResponseDTO> categories = partner.getRestaurantCategories() != null
                ? partner.getRestaurantCategories().stream()
                .map(category -> new RestaurantCategoryResponseDTO(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .collect(Collectors.toList())
                : List.of();

        // Criar e retornar o DTO com todos os dados
        return new PartnerDetailsResponseDTO(
                partner.getId(),
                partner.getName(),
                partner.getCnpj(),
                partner.getPhone(),
                formattedAddress,
                categories
        );
    }
}