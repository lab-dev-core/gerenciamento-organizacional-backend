package com.gestaoformativa.service;

import com.gestaoformativa.model.*;
import com.gestaoformativa.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class SubscriptionService {

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
                .orElseThrow(() -> new RuntimeException("Nenhuma assinatura ativa"));
    }
}