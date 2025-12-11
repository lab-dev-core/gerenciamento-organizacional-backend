package com.gestaoformativa.config;

import com.gestaoformativa.context.TenantContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.lang.reflect.Field;

public class TenantEntityListener {

    @PrePersist
    public void setTenantOnPersist(Object entity) {
        setTenant(entity);
    }

    @PreUpdate
    public void setTenantOnUpdate(Object entity) {
        setTenant(entity);
    }

    private void setTenant(Object entity) {
        Long tenantId = TenantContext.getTenantId();

        // Allow entities to be persisted without tenant during initialization
        if (tenantId == null) {
            // Check if this is a system entity that doesn't require tenant
            if (isSystemEntity(entity)) {
                return;
            }
            throw new IllegalStateException("Tenant ID not set in context. Cannot persist entity without tenant.");
        }

        try {
            Field tenantIdField = findTenantIdField(entity.getClass());
            if (tenantIdField != null) {
                tenantIdField.setAccessible(true);
                Long currentTenantId = (Long) tenantIdField.get(entity);

                // Only set if not already set
                if (currentTenantId == null) {
                    tenantIdField.set(entity, tenantId);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set tenant ID", e);
        }
    }

    private boolean isSystemEntity(Object entity) {
        // These entities don't have tenant_id
        String className = entity.getClass().getSimpleName();
        return className.equals("Tenant") ||
               className.equals("Plan") ||
               className.equals("Role") ||
               className.equals("TenantStatus") ||
               className.equals("SubscriptionStatus");
    }

    private Field findTenantIdField(Class<?> clazz) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField("tenantId");
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
