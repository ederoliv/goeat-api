package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.address.AddressRequestDTO;
import br.com.ederoliv.goeat_api.dto.address.AddressResponseDTO;
import br.com.ederoliv.goeat_api.services.AddressService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_CLIENT')")
    @GetMapping("/clients/{clientId}")
    public ResponseEntity<List<AddressResponseDTO>> getAllClientAddresses(@PathVariable UUID clientId) {
        List<AddressResponseDTO> addresses = addressService.findAllAddressesByClientId(clientId);
        return ResponseEntity.ok(addresses);
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/partners/{partnerId}")
    public ResponseEntity<AddressResponseDTO> getPartnerAddress(@PathVariable UUID partnerId) {
        AddressResponseDTO address = addressService.findPartnerAddress(partnerId);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(address);
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_CLIENT', 'SCOPE_ROLE_PARTNER')")
    @PostMapping
    public ResponseEntity<?> registerAddress(@RequestBody AddressRequestDTO requestDTO) {
        try {
            AddressResponseDTO response = addressService.registerAddress(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao registrar endereço: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_CLIENT', 'SCOPE_ROLE_PARTNER')")
    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable UUID addressId,
            @RequestBody AddressRequestDTO requestDTO) {
        try {
            AddressResponseDTO response = addressService.updateAddress(addressId, requestDTO);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar endereço: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_CLIENT', 'SCOPE_ROLE_PARTNER')")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable UUID addressId) {
        boolean deleted = addressService.deleteAddress(addressId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}