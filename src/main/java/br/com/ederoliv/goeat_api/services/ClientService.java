package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.client.ClientProfileResponseDTO;
import br.com.ederoliv.goeat_api.dto.client.ClientRegisterDTO;
import br.com.ederoliv.goeat_api.dto.client.ClientUpdateDTO;
import br.com.ederoliv.goeat_api.entities.Client;
import br.com.ederoliv.goeat_api.entities.User;
import br.com.ederoliv.goeat_api.repositories.ClientRepository;
import br.com.ederoliv.goeat_api.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final AuthenticationUtilsService authenticationUtilsService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<Client> findByEmail(String email) {
        Optional<User> user = userRepository.findByUsername(email);
        if (user.isPresent() && user.get().getClient() != null) {
            return Optional.of(user.get().getClient());
        }
        return Optional.empty();
    }

    public Optional<Client> findById(UUID id) {
        return clientRepository.findById(id);
    }

    @Transactional
    public Client registerClient(ClientRegisterDTO request) {
        // Validar se já existe usuário com este email
        if (userRepository.existsByUsername(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        // Validar se já existe cliente com este CPF
        if (clientRepository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        // Criar usuário
        User user = new User();
        user.setUsername(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("ROLE_CLIENT");

        // Salvar usuário
        User savedUser = userRepository.save(user);

        // Criar cliente
        Client client = new Client();
        client.setName(request.name());
        client.setCpf(request.cpf());
        client.setPhone(request.phone());
        client.setBirthDate(request.birthDate());
        client.setUser(savedUser);

        // Salvar cliente
        return clientRepository.save(client);
    }

    public ClientProfileResponseDTO getClientProfile(Authentication authentication) {
        UUID clientId = authenticationUtilsService.getClientIdFromAuthentication(authentication);
        if (clientId == null) {
            throw new SecurityException("Não foi possível identificar o cliente autenticado");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        return new ClientProfileResponseDTO(
                client.getId(),
                client.getName(),
                client.getUser().getUsername(), // Email está no User
                client.getCpf(),
                client.getPhone(),
                client.getBirthDate()
        );
    }

    @Transactional
    public ClientProfileResponseDTO updateClientProfile(ClientUpdateDTO updateDTO, Authentication authentication) {
        UUID clientId = authenticationUtilsService.getClientIdFromAuthentication(authentication);
        if (clientId == null) {
            throw new SecurityException("Não foi possível identificar o cliente autenticado");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        if (updateDTO.name() != null && !updateDTO.name().isBlank()) {
            client.setName(updateDTO.name());
        }

        if (updateDTO.phone() != null && !updateDTO.phone().isBlank()) {
            client.setPhone(updateDTO.phone());
        }

        if (updateDTO.birthDate() != null) {
            client.setBirthDate(updateDTO.birthDate());
        }

        Client updatedClient = clientRepository.save(client);

        return new ClientProfileResponseDTO(
                updatedClient.getId(),
                updatedClient.getName(),
                updatedClient.getUser().getUsername(),
                updatedClient.getCpf(),
                updatedClient.getPhone(),
                updatedClient.getBirthDate()
        );
    }
}