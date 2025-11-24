package controller;

import dto.RoleDTO;
import dto.UserDTO;
import model.Role;
import model.User;
import service.RoleService;
import service.UserService;
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

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    // Obter todos os papéis
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar papéis
        if (!currentUser.hasPermission("roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Role> roles = roleService.getAllRoles();

        List<RoleDTO> roleDTOs = roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roleDTOs);
    }

    // Obter um papel específico por ID
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar papéis
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

    // Criar um novo papel
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar papéis
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

    // Atualizar um papel existente
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id,
                                              @Valid @RequestBody RoleDTO roleDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar papéis
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

    // Excluir um papel
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar papéis
        if (!currentUser.hasPermission("roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // Não é possível excluir um papel que está sendo usado por usuários
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // Obter usuários com um papel específico
    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar papéis
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

        // Adicionar contagem de usuários se disponível
        if (role.getUsers() != null) {
            dto.setUserCount(role.getUsers().size());
        } else {
            dto.setUserCount(0);
        }

        return dto;
    }

    private Role convertToEntity(RoleDTO dto) {
        Role role = new Role();

        // Não definimos o ID ao criar uma nova entidade
        // Se estamos atualizando, o ID será definido pelo método de serviço

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
        // Definir apenas os campos básicos para uma visualização resumida
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