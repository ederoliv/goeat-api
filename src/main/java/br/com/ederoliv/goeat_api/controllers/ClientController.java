package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.AuthResponseDTO;
import br.com.ederoliv.goeat_api.dto.client.ClientLoginRequestDTO;
import br.com.ederoliv.goeat_api.dto.client.ClientRegisterDTO;
import br.com.ederoliv.goeat_api.entities.Client;
import br.com.ederoliv.goeat_api.services.AuthenticationService;
import br.com.ederoliv.goeat_api.services.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/clients")
public class ClientController {

    private final ClientService clientService;
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> loginClient(@RequestBody ClientLoginRequestDTO request) {
        try {
            // Tenta autenticar com o serviço de autenticação
            String token = authenticationService.authenticate(request.email(), request.password());

            // Se a autenticação for bem-sucedida, busca os detalhes do cliente
            Client client = clientService.findByEmail(request.email())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            // Constrói a resposta com o token JWT e os detalhes do cliente
            AuthResponseDTO response = new AuthResponseDTO(
                    token,
                    client.getName(),
                    client.getId(),
                    "ROLE_CLIENT"
            );

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar login: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerClient(@RequestBody ClientRegisterDTO request) {
        try {
            Client client = clientService.registerClient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(client);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao registrar cliente: " + e.getMessage());
        }
    }
}