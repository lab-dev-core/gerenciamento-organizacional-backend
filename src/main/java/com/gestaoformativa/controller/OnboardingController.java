package com.gestaoformativa.controller;

import com.gestaoformativa.dto.TenantRegistrationDTO;
import com.gestaoformativa.model.Tenant;
import com.gestaoformativa.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/public/onboarding")
@Tag(name = "Onboarding", description = "Endpoints públicos para registro de novos tenants")
public class OnboardingController {

    private final TenantService tenantService;

    public OnboardingController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Operation(summary = "Registrar novo tenant", description = "Registra uma nova organização no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tenant registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de registro inválidos")
    })
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