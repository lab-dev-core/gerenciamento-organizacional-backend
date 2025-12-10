package com.gestaoformativa.controller;

import com.gestaoformativa.dto.TenantRegistrationDTO;
import com.gestaoformativa.model.Tenant;
import com.gestaoformativa.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
@Tag(name = "Tenant Management", description = "APIs for SaaS tenant management")
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new tenant (public endpoint)",
               description = "Creates a new tenant organization with an admin user and trial subscription")
    public ResponseEntity<?> registerTenant(@Valid @RequestBody TenantRegistrationDTO registration) {
        try {
            logger.info("Tenant registration request for subdomain: {}", registration.getSubdomain());

            Tenant tenant = tenantService.registerTenant(
                registration.getName(),
                registration.getSubdomain(),
                registration.getAdminUsername(),
                registration.getAdminName(),
                registration.getAdminEmail(),
                registration.getAdminPassword(),
                registration.getCity(),
                registration.getState()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tenant registered successfully");
            response.put("tenantId", tenant.getId());
            response.put("subdomain", tenant.getSubdomain());
            response.put("status", tenant.getStatus());

            logger.info("Tenant registered successfully: {} (ID: {})", tenant.getSubdomain(), tenant.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Tenant registration failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            logger.error("Tenant registration error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all tenants (admin only)",
               description = "Retrieves a list of all registered tenants")
    public ResponseEntity<List<Tenant>> listTenants() {
        List<Tenant> tenants = tenantService.findAll();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get tenant by ID (admin only)",
               description = "Retrieves tenant details by ID")
    public ResponseEntity<?> getTenant(@PathVariable Long id) {
        try {
            Tenant tenant = tenantService.findById(id);
            return ResponseEntity.ok(tenant);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspend a tenant (admin only)",
               description = "Suspends tenant access to the platform")
    public ResponseEntity<?> suspendTenant(@PathVariable Long id) {
        try {
            Tenant tenant = tenantService.suspendTenant(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tenant suspended successfully");
            response.put("tenant", tenant);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a tenant (admin only)",
               description = "Activates a suspended or trial tenant")
    public ResponseEntity<?> activateTenant(@PathVariable Long id) {
        try {
            Tenant tenant = tenantService.activateTenant(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tenant activated successfully");
            response.put("tenant", tenant);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel a tenant (admin only)",
               description = "Cancels tenant subscription")
    public ResponseEntity<?> cancelTenant(@PathVariable Long id) {
        try {
            Tenant tenant = tenantService.cancelTenant(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tenant cancelled successfully");
            response.put("tenant", tenant);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/subdomain/{subdomain}")
    @Operation(summary = "Get tenant by subdomain",
               description = "Retrieves tenant details by subdomain")
    public ResponseEntity<?> getTenantBySubdomain(@PathVariable String subdomain) {
        try {
            Tenant tenant = tenantService.findBySubdomain(subdomain);
            return ResponseEntity.ok(tenant);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
