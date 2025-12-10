package com.gestaoformativa.config;

import com.gestaoformativa.context.TenantContext;
import com.gestaoformativa.model.TenantAware;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantEntityListener {

    private static final Logger logger = LoggerFactory.getLogger(TenantEntityListener.class);

    @PrePersist
    public void setTenantOnPersist(TenantAware entity) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            logger.warn("Attempting to persist entity {} without tenant context", entity.getClass().getSimpleName());
            throw new IllegalStateException("Tenant ID not set in context. Cannot persist entity without tenant.");
        }
        entity.setTenantId(tenantId);
        logger.debug("Set tenant_id={} on entity {}", tenantId, entity.getClass().getSimpleName());
    }

    @PreUpdate
    public void setTenantOnUpdate(TenantAware entity) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            logger.warn("Attempting to update entity {} without tenant context", entity.getClass().getSimpleName());
            throw new IllegalStateException("Tenant ID not set in context. Cannot update entity without tenant.");
        }

        // Validate that tenant_id is not being changed
        if (entity.getTenantId() != null && !entity.getTenantId().equals(tenantId)) {
            logger.error("Attempted to change tenant_id from {} to {} on entity {}",
                entity.getTenantId(), tenantId, entity.getClass().getSimpleName());
            throw new IllegalStateException("Cannot change tenant_id of an existing entity");
        }
    }
}
