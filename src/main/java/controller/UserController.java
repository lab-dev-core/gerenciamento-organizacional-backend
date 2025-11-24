package controller;

import dto.UserDTO;
import model.Role;
import model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.RoleRepository;
import repository.UserRepository;
import service.UserService;
import service.RoleService;
import service.MissionLocationService;
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
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MissionLocationService locationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) {
        // Verificar permissões
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (!currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se é o próprio usuário ou se tem permissão
        if (!currentUser.getId().equals(id) && !currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(convertToDTO(user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            User user = convertToEntity(userDTO);
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(createdUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Atualizar um usuário existente
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
                                              @Valid @RequestBody UserDTO userDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se é o próprio usuário ou se tem permissão
        if (!currentUser.getId().equals(id) && !currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            User user = convertToEntity(userDTO);
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(convertToDTO(updatedUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Excluir um usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Apenas administradores podem excluir usuários
        if (!currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Não permitir que um usuário exclua a si mesmo
        if (currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // Atribuir um papel a um usuário
    @PutMapping("/{userId}/role/{roleId}")
    public ResponseEntity<UserDTO> assignRole(@PathVariable Long userId,
                                              @PathVariable Long roleId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            User updatedUser = userService.assignRole(userId, roleId);
            return ResponseEntity.ok(convertToDTO(updatedUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos auxiliares
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        return user;
    }

    @PostMapping("/create-admin-init")
    public ResponseEntity<String> createAdminUserInit() {
        try {
            // Verificar se já existe
            if (userRepository.findByUsername("admin").isPresent()) {
                return ResponseEntity.ok("Usuário admin já existe");
            }

            // Criar role ADMIN
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ADMIN");
                        role.setDescription("Administrador do sistema");
                        role.setCanManageUsers(true);
                        role.setCanManageRoles(true);
                        role.setCanManageStages(true);
                        role.setCanManageDocuments(true);
                        return roleRepository.save(role);
                    });

            // Criar usuário admin
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setName("Administrador");
            adminUser.setRole(adminRole);
            adminUser.setCity("São Paulo");
            adminUser.setState("SP");
            adminUser.setLifeStage(User.LifeStage.CONSECRATED_PERMANENT);
            adminUser.setCommunityYears(0);
            adminUser.setCommunityMonths(0);
            adminUser.setIsEnabled(true);
            adminUser.setIsAccountNonExpired(true);
            adminUser.setIsAccountNonLocked(true);
            adminUser.setIsCredentialsNonExpired(true);

            userRepository.save(adminUser);

            return ResponseEntity.ok("USUÁRIO ADMIN CRIADO COM SUCESSO!\nUsername: admin\nPassword: admin123");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar admin: " + e.getMessage());
        }
    }
}