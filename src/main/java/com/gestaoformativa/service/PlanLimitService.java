package com.gestaoformativa.service;

import com.gestaoformativa.context.TenantContext;
import com.gestaoformativa.model.Plan;
import com.gestaoformativa.repository.FormativeDocumentRepository;
import com.gestaoformativa.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PlanLimitService {

    private static final Logger logger = LoggerFactory.getLogger(PlanLimitService.class);

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final FormativeDocumentRepository documentRepository;

    public PlanLimitService(SubscriptionService subscriptionService,
                           UserRepository userRepository,
                           FormativeDocumentRepository documentRepository) {
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Validates if the tenant can create a new user based on plan limits
     */
    public void validateUserCreation() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Plan plan = subscriptionService.getActivePlan(tenantId);
        long currentUserCount = userRepository.countByTenantId(tenantId);

        if (currentUserCount >= plan.getMaxUsers()) {
            logger.warn("Tenant {} exceeded user limit. Current: {}, Max: {}",
                       tenantId, currentUserCount, plan.getMaxUsers());
            throw new PlanLimitExceededException(
                String.format("User limit exceeded. Current plan allows maximum %d users. " +
                             "Please upgrade your plan to add more users.", plan.getMaxUsers())
            );
        }

        logger.debug("User creation validation passed for tenant {}. Count: {}/{}",
                    tenantId, currentUserCount, plan.getMaxUsers());
    }

    /**
     * Validates if the tenant can create a new document based on plan limits
     */
    public void validateDocumentCreation() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Plan plan = subscriptionService.getActivePlan(tenantId);
        long currentDocumentCount = documentRepository.countByTenantId(tenantId);

        if (currentDocumentCount >= plan.getMaxDocuments()) {
            logger.warn("Tenant {} exceeded document limit. Current: {}, Max: {}",
                       tenantId, currentDocumentCount, plan.getMaxDocuments());
            throw new PlanLimitExceededException(
                String.format("Document limit exceeded. Current plan allows maximum %d documents. " +
                             "Please upgrade your plan to add more documents.", plan.getMaxDocuments())
            );
        }

        logger.debug("Document creation validation passed for tenant {}. Count: {}/{}",
                    tenantId, currentDocumentCount, plan.getMaxDocuments());
    }

    /**
     * Validates if the tenant can upload a file based on storage limits
     */
    public void validateStorageLimit(long fileSizeBytes) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Plan plan = subscriptionService.getActivePlan(tenantId);
        long maxStorageBytes = plan.getMaxStorageMb() * 1024 * 1024; // Convert MB to bytes
        long currentStorageBytes = documentRepository.sumAttachmentSizeByTenantId(tenantId);
        long newTotalStorage = currentStorageBytes + fileSizeBytes;

        if (newTotalStorage > maxStorageBytes) {
            logger.warn("Tenant {} exceeded storage limit. Current: {} MB, Attempting: {} MB, Max: {} MB",
                       tenantId,
                       currentStorageBytes / (1024 * 1024),
                       fileSizeBytes / (1024 * 1024),
                       plan.getMaxStorageMb());
            throw new PlanLimitExceededException(
                String.format("Storage limit exceeded. Current plan allows maximum %d MB. " +
                             "Please upgrade your plan to increase storage.", plan.getMaxStorageMb())
            );
        }

        logger.debug("Storage validation passed for tenant {}. Current: {} MB, Max: {} MB",
                    tenantId, currentStorageBytes / (1024 * 1024), plan.getMaxStorageMb());
    }

    /**
     * Gets current usage statistics for the tenant
     */
    public UsageStatistics getUsageStatistics(Long tenantId) {
        Plan plan = subscriptionService.getActivePlan(tenantId);

        long userCount = userRepository.countByTenantId(tenantId);
        long documentCount = documentRepository.countByTenantId(tenantId);
        long storageBytes = documentRepository.sumAttachmentSizeByTenantId(tenantId);
        long storageMb = storageBytes / (1024 * 1024);

        return new UsageStatistics(
            userCount, plan.getMaxUsers(),
            documentCount, plan.getMaxDocuments(),
            storageMb, plan.getMaxStorageMb()
        );
    }

    /**
     * Exception thrown when plan limits are exceeded
     */
    public static class PlanLimitExceededException extends RuntimeException {
        public PlanLimitExceededException(String message) {
            super(message);
        }
    }

    /**
     * DTO for usage statistics
     */
    public static class UsageStatistics {
        public final long userCount;
        public final int maxUsers;
        public final long documentCount;
        public final int maxDocuments;
        public final long storageMb;
        public final long maxStorageMb;

        public UsageStatistics(long userCount, int maxUsers,
                              long documentCount, int maxDocuments,
                              long storageMb, long maxStorageMb) {
            this.userCount = userCount;
            this.maxUsers = maxUsers;
            this.documentCount = documentCount;
            this.maxDocuments = maxDocuments;
            this.storageMb = storageMb;
            this.maxStorageMb = maxStorageMb;
        }

        public double getUserPercentage() {
            return maxUsers > 0 ? (userCount * 100.0) / maxUsers : 0;
        }

        public double getDocumentPercentage() {
            return maxDocuments > 0 ? (documentCount * 100.0) / maxDocuments : 0;
        }

        public double getStoragePercentage() {
            return maxStorageMb > 0 ? (storageMb * 100.0) / maxStorageMb : 0;
        }
    }
}
