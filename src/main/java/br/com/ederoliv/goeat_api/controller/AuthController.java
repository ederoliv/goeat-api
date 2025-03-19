package br.com.ederoliv.goeat_api.controller;

import br.com.ederoliv.goeat_api.dto.clients.ClientLoginRequestDTO;
import br.com.ederoliv.goeat_api.dto.clients.ClientRequestDTO;
import br.com.ederoliv.goeat_api.dto.partners.PartnerLoginRequestDTO;
import br.com.ederoliv.goeat_api.dto.partners.PartnerRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    @PostMapping("/partners")
    public ResponseEntity<?> register(@RequestBody PartnerRequestDTO request){
        return ResponseEntity.ok().build();
    }

    @PostMapping("/partners/login")
    public ResponseEntity<?> login(@RequestBody PartnerLoginRequestDTO request){
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clients")
    public ResponseEntity<?> register(@RequestBody ClientRequestDTO request){
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clients/login")
    public ResponseEntity<?> login(@RequestBody ClientLoginRequestDTO request){
        return ResponseEntity.ok().build();
    }
}
