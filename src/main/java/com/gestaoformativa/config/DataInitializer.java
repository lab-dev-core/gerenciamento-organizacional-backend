package com.gestaoformativa.config;

import com.gestaoformativa.model.Role;
import com.gestaoformativa.model.User;
import com.gestaoformativa.model.User.LifeStage;
import com.gestaoformativa.repository.RoleRepository;
import com.gestaoformativa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Iniciando configuração de dados padrão...");

        // Criar role de ADMIN
        Role adminRole = createAdminRoleIfNotExists();

        // Criar usuário admin
        createAdminUserIfNotExists(adminRole);

        log.info("Configuração de dados padrão concluída!");
    }

    private Role createAdminRoleIfNotExists() {
        Optional<Role> existingAdminRole = roleRepository.findByName("ADMIN");

        if (existingAdminRole.isPresent()) {
            log.info("Role ADMIN já existe");
            return existingAdminRole.get();
        }

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrador do sistema");
        adminRole.setCanManageUsers(true);
        adminRole.setCanManageRoles(true);
        adminRole.setCanManageStages(true);
        adminRole.setCanManageDocuments(true);

        Role savedRole = roleRepository.save(adminRole);
        log.info("Role ADMIN criada com sucesso!");
        return savedRole;
    }

    private void createAdminUserIfNotExists(Role adminRole) {
        Optional<User> existingAdmin = userRepository.findByUsername("admin");

        if (existingAdmin.isPresent()) {
            log.info("Usuário admin já existe");
            return;
        }

        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setName("Administrador");
        adminUser.setAge(30);
        adminUser.setRole(adminRole);
        adminUser.setCity("Cidade Padrão");
        adminUser.setState("Estado Padrão");
        adminUser.setLifeStage(LifeStage.CONSECRATED_PERMANENT);
        adminUser.setCommunityYears(0);
        adminUser.setCommunityMonths(0);
        adminUser.setIsEnabled(true);
        adminUser.setIsAccountNonExpired(true);
        adminUser.setIsAccountNonLocked(true);
        adminUser.setIsCredentialsNonExpired(true);

        userRepository.save(adminUser);
        log.info("✅ Usuário admin criado com sucesso!");
        log.info("📌 Login: admin | Senha: admin123");
    }
}