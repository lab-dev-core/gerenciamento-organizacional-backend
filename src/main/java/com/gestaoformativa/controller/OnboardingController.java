package com.gestaoformativa.controller;

import com.gestaoformativa.dto.TenantRegistrationDTO;
import com.gestaoformativa.model.Tenant;
import com.gestaoformativa.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/onboarding")
public class OnboardingController {

    private final TenantService tenantService;

    public OnboardingController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerTenant(@RequestBody TenantRegistrationDTO dto) {
        try {
            Tenant tenant = tenantService.createTenant(
                    dto.getName(),
                    dto.getSubdomain(),
                    dto.getAdminEmail(),
                    dto.getAdminPassword()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}