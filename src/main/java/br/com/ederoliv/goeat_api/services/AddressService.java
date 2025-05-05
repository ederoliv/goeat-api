package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.address.AddressRequestDTO;
import br.com.ederoliv.goeat_api.dto.address.AddressResponseDTO;
import br.com.ederoliv.goeat_api.entities.Address;
import br.com.ederoliv.goeat_api.entities.Client;
import br.com.ederoliv.goeat_api.entities.Partner;
import br.com.ederoliv.goeat_api.repositories.AddressRepository;
import br.com.ederoliv.goeat_api.repositories.ClientRepository;
import br.com.ederoliv.goeat_api.repositories.PartnerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final ClientRepository clientRepository;
    private final PartnerRepository partnerRepository;

    public List<AddressResponseDTO> findAllAddressesByClientId(UUID clientId) {
        Optional<List<Address>> addresses = addressRepository.findByClientId(clientId);

        if (addresses.isEmpty() || addresses.get().isEmpty()) {
            return List.of();
        }

        return addresses.get().stream()
                .map(address -> new AddressResponseDTO(
                        address.getId(),
                        address.getStreet(),
                        address.getNumber(),
                        address.getComplement(),
                        address.getNeighborhood(),
                        address.getCity(),
                        address.getState(),
                        address.getZipCode(),
                        address.getReference(),
                        address.getClient().getId(),
                        "CLIENT"
                ))
                .collect(Collectors.toList());
    }

    public String getFullAddress(UUID partnerId) {
        // Busca o endereço do parceiro
        Address address = addressRepository.findByPartnerId(partnerId)
                .orElse(null);

        if (address == null) {
            return null;
        }

        StringBuilder formattedAddress = new StringBuilder();

        formattedAddress.append(address.getStreet());

        if (address.getNumber() != null && !address.getNumber().isEmpty()) {
            formattedAddress.append(", nº ").append(address.getNumber());
        }

        if (address.getComplement() != null && !address.getComplement().isEmpty()) {
            formattedAddress.append(", ").append(address.getComplement());
        }

        if (address.getNeighborhood() != null && !address.getNeighborhood().isEmpty()) {
            formattedAddress.append(", ").append(address.getNeighborhood());
        }

        if (address.getCity() != null && !address.getCity().isEmpty()) {
            formattedAddress.append(", ").append(address.getCity());

            if (address.getState() != null && !address.getState().isEmpty()) {
                formattedAddress.append("/").append(address.getState());
            }
        }

        if (address.getZipCode() != null && !address.getZipCode().isEmpty()) {
            formattedAddress.append(", CEP: ").append(address.getZipCode());
        }

        if (address.getReference() != null && !address.getReference().isEmpty()) {
            formattedAddress.append(", Referência: ").append(address.getReference());
        }

        return formattedAddress.toString();
    }

    public AddressResponseDTO registerPartnerAddress(AddressRequestDTO requestDTO, UUID partnerId) {
        // Cria um novo DTO com o partnerId fornecido, garantindo que o clientId seja null
        AddressRequestDTO secureRequest = new AddressRequestDTO(
                requestDTO.street(),
                requestDTO.number(),
                requestDTO.complement(),
                requestDTO.neighborhood(),
                requestDTO.city(),
                requestDTO.state(),
                requestDTO.zipCode(),
                requestDTO.reference(),
                null, // Sempre será null para partners
                partnerId // Usa o ID do parceiro fornecido
        );

        // Utiliza o método existente para registrar o endereço
        return registerAddress(secureRequest);
    }


    public AddressResponseDTO registerAddress(AddressRequestDTO requestDTO) {
        Address address = new Address();
        address.setStreet(requestDTO.street());
        address.setNumber(requestDTO.number());
        address.setComplement(requestDTO.complement());
        address.setNeighborhood(requestDTO.neighborhood());
        address.setCity(requestDTO.city());
        address.setState(requestDTO.state());
        address.setZipCode(requestDTO.zipCode());
        address.setReference(requestDTO.reference());

        // Verifica se é um cliente ou parceiro
        if (requestDTO.clientId() != null) {
            Client client = clientRepository.findById(requestDTO.clientId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            address.setClient(client);
            address.setPartner(null);
        } else if (requestDTO.partnerId() != null) {
            Partner partner = partnerRepository.findById(requestDTO.partnerId())
                    .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

            // Verifica se o parceiro já tem um endereço
            Optional<Address> existingAddress = addressRepository.findByPartnerId(partner.getId());
            if (existingAddress.isPresent()) {
                throw new IllegalStateException("Este parceiro já possui um endereço cadastrado");
            }

            address.setPartner(partner);
            address.setClient(null);
        } else {
            throw new IllegalArgumentException("É necessário informar o ID do cliente ou do parceiro");
        }

        Address savedAddress = addressRepository.save(address);

        String ownerType = address.getClient() != null ? "CLIENT" : "PARTNER";
        UUID ownerId = address.getClient() != null ? address.getClient().getId() : address.getPartner().getId();

        return new AddressResponseDTO(
                savedAddress.getId(),
                savedAddress.getStreet(),
                savedAddress.getNumber(),
                savedAddress.getComplement(),
                savedAddress.getNeighborhood(),
                savedAddress.getCity(),
                savedAddress.getState(),
                savedAddress.getZipCode(),
                savedAddress.getReference(),
                ownerId,
                ownerType
        );
    }

    public AddressResponseDTO updateAddress(UUID addressId, AddressRequestDTO requestDTO) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        address.setStreet(requestDTO.street());
        address.setNumber(requestDTO.number());
        address.setComplement(requestDTO.complement());
        address.setNeighborhood(requestDTO.neighborhood());
        address.setCity(requestDTO.city());
        address.setState(requestDTO.state());
        address.setZipCode(requestDTO.zipCode());
        address.setReference(requestDTO.reference());

        // Se o endereço mudar de dono
        if (requestDTO.clientId() != null &&
                (address.getClient() == null || !address.getClient().getId().equals(requestDTO.clientId()))) {
            Client client = clientRepository.findById(requestDTO.clientId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            address.setClient(client);
            address.setPartner(null);
        } else if (requestDTO.partnerId() != null &&
                (address.getPartner() == null || !address.getPartner().getId().equals(requestDTO.partnerId()))) {
            Partner partner = partnerRepository.findById(requestDTO.partnerId())
                    .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

            // Se o endereço já pertencia a outro parceiro
            if (address.getPartner() != null && !address.getPartner().getId().equals(partner.getId())) {
                // Verificar se o novo parceiro já tem um endereço
                Optional<Address> existingAddress = addressRepository.findByPartnerId(partner.getId());
                if (existingAddress.isPresent()) {
                    throw new IllegalStateException("Este parceiro já possui um endereço cadastrado");
                }
            }

            address.setPartner(partner);
            address.setClient(null);
        }

        Address updatedAddress = addressRepository.save(address);

        String ownerType = updatedAddress.getClient() != null ? "CLIENT" : "PARTNER";
        UUID ownerId = updatedAddress.getClient() != null ?
                updatedAddress.getClient().getId() : updatedAddress.getPartner().getId();

        return new AddressResponseDTO(
                updatedAddress.getId(),
                updatedAddress.getStreet(),
                updatedAddress.getNumber(),
                updatedAddress.getComplement(),
                updatedAddress.getNeighborhood(),
                updatedAddress.getCity(),
                updatedAddress.getState(),
                updatedAddress.getZipCode(),
                updatedAddress.getReference(),
                ownerId,
                ownerType
        );
    }

    public boolean deleteAddress(UUID addressId) {
        if (addressRepository.existsById(addressId)) {
            addressRepository.deleteById(addressId);
            return true;
        }
        return false;
    }
}