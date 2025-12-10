package com.gestaoformativa.service;

import com.gestaoformativa.context.TenantContext;
import com.gestaoformativa.model.*;
import com.gestaoformativa.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public TenantService(TenantRepository tenantRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         RoleRepository roleRepository,
                         PlanRepository planRepository,
                         SubscriptionRepository subscriptionRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public Tenant registerTenant(String name, String subdomain, String adminUsername,
                                  String adminName, String adminEmail, String adminPassword,
                                  String city, String state) {
        logger.info("Registering new tenant with subdomain: {}", subdomain);

        // Validar subdomínio
        validateSubdomain(subdomain);

        if (tenantRepository.existsBySubdomain(subdomain)) {
            throw new IllegalArgumentException("Subdomain already exists: " + subdomain);
        }

        // Criar o tenant (sem contexto, pois é uma operação especial)
        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setSubdomain(subdomain);
        tenant.setStatus(TenantStatus.TRIAL);
        tenant = tenantRepository.save(tenant);

        // Configurar contexto de tenant para criar os dados relacionados
        TenantContext.setTenantId(tenant.getId());

        try {
            // Criar role ADMIN para o tenant
            Role adminRole = createAdminRole(tenant.getId());

            // Criar plano trial padrão
            Plan trialPlan = getOrCreateTrialPlan();

            // Criar assinatura inicial (30 dias de trial)
            createTrialSubscription(tenant, trialPlan);

            // Criar usuário admin
            createAdminUser(tenant.getId(), adminRole, adminUsername, adminName,
                           adminEmail, adminPassword, city, state);

            logger.info("Tenant {} registered successfully with ID: {}", subdomain, tenant.getId());
            return tenant;
        } finally {
            TenantContext.clear();
        }
    }

    private void validateSubdomain(String subdomain) {
        if (subdomain == null || subdomain.trim().isEmpty()) {
            throw new IllegalArgumentException("Subdomain cannot be empty");
        }

        // Validar formato do subdomínio (apenas letras minúsculas, números e hífens)
        if (!subdomain.matches("^[a-z0-9][a-z0-9-]*[a-z0-9]$")) {
            throw new IllegalArgumentException("Subdomain must contain only lowercase letters, numbers and hyphens, and cannot start or end with a hyphen");
        }

        // Validar comprimento
        if (subdomain.length() < 3 || subdomain.length() > 63) {
            throw new IllegalArgumentException("Subdomain must be between 3 and 63 characters");
        }

        // Subdomínios reservados
        List<String> reserved = List.of("www", "api", "admin", "app", "mail", "ftp", "localhost", "test");
        if (reserved.contains(subdomain.toLowerCase())) {
            throw new IllegalArgumentException("Subdomain is reserved");
        }
    }

    private Role createAdminRole(Long tenantId) {
        Role adminRole = new Role();
        adminRole.setTenantId(tenantId);
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator with full permissions");
        adminRole.setCanManageUsers(true);
        adminRole.setCanManageRoles(true);
        adminRole.setCanManageStages(true);
        adminRole.setCanManageDocuments(true);
        return roleRepository.save(adminRole);
    }

    private Plan getOrCreateTrialPlan() {
        // Buscar plano trial existente (criado manualmente ou em migration)
        Optional<Plan> existingPlan = planRepository.findByActiveTrue().stream()
            .filter(p -> "TRIAL".equalsIgnoreCase(p.getName()))
            .findFirst();

        if (existingPlan.isPresent()) {
            return existingPlan.get();
        }

        // Se não existe, criar plano trial padrão
        Plan trialPlan = new Plan();
        trialPlan.setName("TRIAL");
        trialPlan.setDescription("Trial plan - 30 days free");
        trialPlan.setPrice(java.math.BigDecimal.ZERO);
        trialPlan.setMaxUsers(5);
        trialPlan.setMaxDocuments(50);
        trialPlan.setMaxStorageMb(500L);
        trialPlan.setActive(true);
        return planRepository.save(trialPlan);
    }

    private void createTrialSubscription(Tenant tenant, Plan plan) {
        Subscription subscription = new Subscription();
        subscription.setTenant(tenant);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(30)); // 30 dias de trial
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);
    }

    private void createAdminUser(Long tenantId, Role adminRole, String username, String name,
                                 String email, String password, String city, String state) {
        User adminUser = new User();
        adminUser.setTenantId(tenantId);
        adminUser.setUsername(username);
        adminUser.setName(name);
        adminUser.setEmail(email);
        adminUser.setPassword(passwordEncoder.encode(password));
        adminUser.setRole(adminRole);
        adminUser.setCity(city);
        adminUser.setState(state);
        adminUser.setIsEnabled(true);
        adminUser.setIsAccountNonExpired(true);
        adminUser.setIsAccountNonLocked(true);
        adminUser.setIsCredentialsNonExpired(true);
        adminUser.setLifeStage(User.LifeStage.DISCIPLESHIP); // Valor padrão
        adminUser.setCommunityYears(0);
        adminUser.setCommunityMonths(0);
        userRepository.save(adminUser);
    }

    public Tenant findById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + id));
    }

    public Tenant findBySubdomain(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with subdomain: " + subdomain));
    }

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    @Transactional
    public Tenant suspendTenant(Long tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.setStatus(TenantStatus.SUSPENDED);
        logger.info("Tenant {} suspended", tenant.getSubdomain());
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant activateTenant(Long tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.setStatus(TenantStatus.ACTIVE);
        logger.info("Tenant {} activated", tenant.getSubdomain());
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant cancelTenant(Long tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.setStatus(TenantStatus.CANCELLED);
        logger.info("Tenant {} cancelled", tenant.getSubdomain());
        return tenantRepository.save(tenant);
    }

    public boolean isTenantActive(Long tenantId) {
        Tenant tenant = findById(tenantId);
        return tenant.getStatus() == TenantStatus.ACTIVE || tenant.getStatus() == TenantStatus.TRIAL;
    }
}