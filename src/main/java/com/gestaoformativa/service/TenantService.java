package com.gestaoformativa.service;

import com.gestaoformativa.model.Tenant;
import com.gestaoformativa.model.TenantStatus;
import com.gestaoformativa.model.User;
import com.gestaoformativa.repository.TenantRepository;
import com.gestaoformativa.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantService(TenantRepository tenantRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Tenant createTenant(String name, String subdomain, String adminEmail, String adminPassword) {
        if (tenantRepository.existsBySubdomain(subdomain)) {
            throw new RuntimeException("Subdomínio já existe");
        }

        // Criar o tenant
        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setSubdomain(subdomain);
        tenant.setStatus(TenantStatus.TRIAL);
        tenant = tenantRepository.save(tenant);

        // Criar usuário admin
        User adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
//        adminUser.setTenant(tenant);
//        adminUser.setRole("ROLE_ADMIN");
//        adminUser.setActive(true);
        userRepository.save(adminUser);

        return tenant;
    }

    public Tenant findById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
    }

    public Tenant findBySubdomain(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
    }
}