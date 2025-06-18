package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.partner.PartnerRequestDTO;
import br.com.ederoliv.goeat_api.dto.partner.PartnerResponseDTO;
import br.com.ederoliv.goeat_api.dto.partner.PartnerWithCategoriesResponseDTO;
import br.com.ederoliv.goeat_api.dto.restaurantCategory.RestaurantCategoryResponseDTO;
import br.com.ederoliv.goeat_api.entities.Menu;
import br.com.ederoliv.goeat_api.entities.Partner;
import br.com.ederoliv.goeat_api.entities.User;
import br.com.ederoliv.goeat_api.repositories.MenuRepository;
import br.com.ederoliv.goeat_api.repositories.PartnerRepository;
import br.com.ederoliv.goeat_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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
}