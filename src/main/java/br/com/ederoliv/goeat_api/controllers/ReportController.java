package br.com.ederoliv.goeat_api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/reports")
public class ReportController {



    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyReport() {
        return ResponseEntity.ok("43789");
    }


    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyReport() {
        return ResponseEntity.ok("120000");
    }


    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport() {
        return ResponseEntity.ok("120000000");
    }


}
