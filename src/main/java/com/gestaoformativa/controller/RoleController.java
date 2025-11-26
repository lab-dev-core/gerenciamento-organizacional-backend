package com.gestaoformativa.controller;

import com.gestaoformativa.dto.RoleDTO;
import com.gestaoformativa.dto.UserDTO;
import com.gestaoformativa.model.Role;
import com.gestaoformativa.model.User;
import com.gestaoformativa.service.RoleService;
import com.gestaoformativa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Papéis", description = "Gerenciamento de papéis e permissões")
@SecurityRequirement(name = "bearer-jwt")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Listar papéis", description = "Retorna todos os papéis do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Papéis listados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar papéis")
    })
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Role> roles = roleService.getAllRoles();

        List<RoleDTO> roleDTOs = roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roleDTOs);
    }

    @Operation(summary = "Obter papel por ID", description = "Retorna um papel específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Papel encontrado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar papéis"),
            @ApiResponse(responseCode = "404", description = "Papel não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Role role = roleService.getRoleById(id);
            return ResponseEntity.ok(convertToDTO(role));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Criar papel", description = "Cria um novo papel no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Papel criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar papéis")
    })
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Role role = convertToEntity(roleDTO);
            Role createdRole = roleService.createRole(role);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(createdRole));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Atualizar papel", description = "Atualiza um papel existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Papel atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar papéis"),
            @ApiResponse(responseCode = "404", description = "Papel não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id,
                                              @Valid @RequestBody RoleDTO roleDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Role role = convertToEntity(roleDTO);
            Role updatedRole = roleService.updateRole(id, role);
            return ResponseEntity.ok(convertToDTO(updatedRole));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Excluir papel", description = "Exclui um papel existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Papel excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar papéis"),
            @ApiResponse(responseCode = "404", description = "Papel não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito - papel não pode ser excluído")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Usuários do papel", description = "Retorna usuários associados a um papel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar papéis"),
            @ApiResponse(responseCode = "404", description = "Papel não encontrado")
    })
    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Role role = roleService.getRoleById(id);

            List<UserDTO> userDTOs = role.getUsers().stream()
                    .map(this::convertUserToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userDTOs);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos auxiliares
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setCanManageUsers(role.getCanManageUsers());
        dto.setCanManageRoles(role.getCanManageRoles());
        dto.setCanManageStages(role.getCanManageStages());
        dto.setCanManageDocuments(role.getCanManageDocuments());

        if (role.getUsers() != null) {
            dto.setUserCount(role.getUsers().size());
        } else {
            dto.setUserCount(0);
        }

        return dto;
    }

    private Role convertToEntity(RoleDTO dto) {
        Role role = new Role();

        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setCanManageUsers(dto.getCanManageUsers());
        role.setCanManageRoles(dto.getCanManageRoles());
        role.setCanManageStages(dto.getCanManageStages());
        role.setCanManageDocuments(dto.getCanManageDocuments());

        return role;
    }

    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());

        if (user.getMissionLocation() != null) {
            dto.setMissionLocationId(user.getMissionLocation().getId());
            dto.setMissionLocationName(user.getMissionLocation().getName());
        }

        dto.setLifeStage(user.getLifeStage());
        dto.setFullLocation(user.getFullLocation());

        return dto;
    }
}