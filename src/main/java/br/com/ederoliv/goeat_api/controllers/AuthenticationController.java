package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.config.UserAuthenticated;
import br.com.ederoliv.goeat_api.dto.AuthResponseDTO;
import br.com.ederoliv.goeat_api.entities.User;
import br.com.ederoliv.goeat_api.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("authenticate")
    public ResponseEntity<AuthResponseDTO> authenticate(Authentication authentication) {
        String token = authenticationService.authenticate(authentication);

        // Extrair informações adicionais para a resposta
        User user = null;
        if (authentication.getPrincipal() instanceof UserAuthenticated) {
            user = ((UserAuthenticated) authentication.getPrincipal()).getUser();
        }

        if (user != null) {
            String name;
            UUID id;

            if (user.getRole().equals("ROLE_CLIENT") && user.getClient() != null) {
                name = user.getClient().getName();
                id = user.getClient().getId();
            } else if (user.getRole().equals("ROLE_PARTNER") && user.getPartner() != null) {
                name = user.getPartner().getName();
                id = user.getPartner().getId();
            } else {
                name = user.getUsername();
                id = user.getId();
            }

            return ResponseEntity.ok(new AuthResponseDTO(token, name, id, user.getRole()));
        }

        // Caso não tenhamos as informações detalhadas
        return ResponseEntity.ok(new AuthResponseDTO(token, null, null, null));
    }
}