package com.gestaoformativa.repository;

import com.gestaoformativa.model.Subscription;
import com.gestaoformativa.model.SubscriptionStatus;
import com.gestaoformativa.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByTenantAndStatus(Tenant tenant, SubscriptionStatus status);
}
