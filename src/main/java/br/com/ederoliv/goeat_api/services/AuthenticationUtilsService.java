package br.com.ederoliv.goeat_api.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationUtilsService {

    public UUID getPartnerIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String partnerIdStr = jwt.getClaim("partnerId");
            if (partnerIdStr != null && !partnerIdStr.isEmpty()) {
                return UUID.fromString(partnerIdStr);
            }
        }
        return null;
    }

    public UUID getClientIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String clientIdStr = jwt.getClaim("clientId");
            if (clientIdStr != null && !clientIdStr.isEmpty()) {
                return UUID.fromString(clientIdStr);
            }
        }
        return null;
    }

    public UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userIdStr = jwt.getClaim("userId");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                return UUID.fromString(userIdStr);
            }
        }
        return null;
    }

    public String getUserRole(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String scope = jwt.getClaim("scope");
            if (scope != null) {
                if (scope.contains("ROLE_CLIENT")) {
                    return "ROLE_CLIENT";
                } else if (scope.contains("ROLE_PARTNER")) {
                    return "ROLE_PARTNER";
                }
            }
        }
        return null;
    }
}