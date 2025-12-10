package com.gestaoformativa.service;

import com.gestaoformativa.model.*;
import com.gestaoformativa.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final TenantService tenantService;
    private final PlanService planService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               TenantService tenantService,
                               PlanService planService) {
        this.subscriptionRepository = subscriptionRepository;
        this.tenantService = tenantService;
        this.planService = planService;
    }

    @Transactional
    public Subscription createSubscription(Long tenantId, Long planId) {
        Tenant tenant = tenantService.findById(tenantId);
        Plan plan = planService.findById(planId);

        Subscription subscription = new Subscription();
        subscription.setTenant(tenant);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        tenant.setStatus(TenantStatus.ACTIVE);

        return subscriptionRepository.save(subscription);
    }

    public Subscription findActiveTenantSubscription(Long tenantId) {
        Tenant tenant = tenantService.findById(tenantId);
        return subscriptionRepository.findByTenantAndStatus(tenant, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No active subscription found"));
    }

    @Transactional
    public Subscription updateSubscription(Long tenantId, Long newPlanId) {
        logger.info("Updating subscription for tenant {} to plan {}", tenantId, newPlanId);

        Tenant tenant = tenantService.findById(tenantId);
        Plan newPlan = planService.findById(newPlanId);

        // Cancelar assinatura atual se existir
        Optional<Subscription> currentSubscription = subscriptionRepository
            .findByTenantAndStatus(tenant, SubscriptionStatus.ACTIVE);

        if (currentSubscription.isPresent()) {
            Subscription current = currentSubscription.get();
            current.setStatus(SubscriptionStatus.CANCELLED);
            current.setEndDate(LocalDateTime.now());
            subscriptionRepository.save(current);
            logger.info("Cancelled previous subscription for tenant {}", tenantId);
        }

        // Criar nova assinatura
        Subscription newSubscription = new Subscription();
        newSubscription.setTenant(tenant);
        newSubscription.setPlan(newPlan);
        newSubscription.setStartDate(LocalDateTime.now());
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);

        // Definir data de fim baseada no tipo de plano
        if ("TRIAL".equalsIgnoreCase(newPlan.getName())) {
            newSubscription.setEndDate(LocalDateTime.now().plusDays(30));
            tenant.setStatus(TenantStatus.TRIAL);
        } else {
            newSubscription.setEndDate(LocalDateTime.now().plusMonths(1));
            tenant.setStatus(TenantStatus.ACTIVE);
        }

        Subscription saved = subscriptionRepository.save(newSubscription);
        logger.info("Created new subscription {} for tenant {}", saved.getId(), tenantId);
        return saved;
    }

    public Plan getActivePlan(Long tenantId) {
        Subscription subscription = findActiveTenantSubscription(tenantId);
        return subscription.getPlan();
    }

    @Transactional
    public void cancelSubscription(Long tenantId) {
        logger.info("Cancelling subscription for tenant {}", tenantId);

        Tenant tenant = tenantService.findById(tenantId);
        Optional<Subscription> activeSubscription = subscriptionRepository
            .findByTenantAndStatus(tenant, SubscriptionStatus.ACTIVE);

        if (activeSubscription.isPresent()) {
            Subscription subscription = activeSubscription.get();
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setEndDate(LocalDateTime.now());
            subscriptionRepository.save(subscription);

            tenant.setStatus(TenantStatus.CANCELLED);
            logger.info("Subscription cancelled for tenant {}", tenantId);
        } else {
            throw new IllegalArgumentException("No active subscription found to cancel");
        }
    }

    public boolean isSubscriptionExpired(Long tenantId) {
        try {
            Subscription subscription = findActiveTenantSubscription(tenantId);
            return subscription.getEndDate() != null &&
                   subscription.getEndDate().isBefore(LocalDateTime.now());
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
