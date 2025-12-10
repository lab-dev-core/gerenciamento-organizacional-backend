package com.gestaoformativa.controller;

import com.gestaoformativa.dto.JwtResponse;
import com.gestaoformativa.dto.LoginRequest;
import com.gestaoformativa.model.Tenant;
import com.gestaoformativa.model.User;
import com.gestaoformativa.service.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.gestaoformativa.config.JwtTokenProvider;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação de usuários")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TenantService tenantService;

    @Operation(summary = "Autenticar usuário", description = "Realiza login e retorna token JWT com validação de tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "403", description = "Usuário não pertence ao tenant especificado ou tenant inativo")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Validar se o tenant existe e está ativo
            Tenant tenant = tenantService.findById(loginRequest.getTenantId());
            if (!tenantService.isTenantActive(loginRequest.getTenantId())) {
                logger.warn("Login attempt for inactive tenant: {}", loginRequest.getTenantId());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Tenant is suspended or inactive");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Autenticar usuário
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User userDetails = (User) authentication.getPrincipal();

            // Validar se o usuário pertence ao tenant especificado
            if (!userDetails.getTenantId().equals(loginRequest.getTenantId())) {
                logger.warn("User {} attempted to login to wrong tenant. User tenant: {}, Requested tenant: {}",
                           userDetails.getUsername(), userDetails.getTenantId(), loginRequest.getTenantId());
                Map<String, String> error = new HashMap<>();
                error.put("error", "User does not belong to the specified tenant");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Gerar token JWT
            String jwt = jwtTokenProvider.generateToken(authentication);

            logger.info("User {} successfully authenticated for tenant {}",
                       userDetails.getUsername(), loginRequest.getTenantId());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getName(),
                    userDetails.getRole().getName(),
                    userDetails.getTenantId(),
                    tenant.getName()));

        } catch (IllegalArgumentException e) {
            logger.error("Tenant not found: {}", loginRequest.getTenantId());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Tenant not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            logger.error("Login error", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
