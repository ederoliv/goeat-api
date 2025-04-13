package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.config.UserAuthenticated;
import br.com.ederoliv.goeat_api.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private final JwtEncoder encoder;

    public JwtService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        long expiry = 1800L; //  30 minutos até expirar

        // Obtém as authorities do usuário
        String scopes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        // Se for nossa classe personalizada UserAuthenticated, extraímos mais informações
        Map<String, Object> additionalClaims = new HashMap<>();
        if (authentication.getPrincipal() instanceof UserAuthenticated) {
            User user = ((UserAuthenticated) authentication.getPrincipal()).getUser();
            additionalClaims.put("userId", user.getId().toString());

            // Dependendo do papel, adicionamos o ID específico
            if (user.getRole().equals("ROLE_CLIENT") && user.getClient() != null) {
                additionalClaims.put("clientId", user.getClient().getId().toString());
            } else if (user.getRole().equals("ROLE_PARTNER") && user.getPartner() != null) {
                additionalClaims.put("partnerId", user.getPartner().getId().toString());
            }
        }

        var claims = JwtClaimsSet.builder()
                .issuer("goeat-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(authentication.getName())
                .claim("scope", scopes)
                .claims(existingClaims -> existingClaims.putAll(additionalClaims))
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}