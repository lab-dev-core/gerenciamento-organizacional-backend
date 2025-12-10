package com.gestaoformativa.repository;

import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.MissionLocation;
import com.gestaoformativa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FormativeDocumentRepository extends JpaRepository<FormativeDocument, Long> {

    List<FormativeDocument> findByTitleContainingIgnoreCase(String title);

    List<FormativeDocument> findByAuthor(User author);

    List<FormativeDocument> findByDocumentType(FormativeDocument.DocumentType documentType);

    List<FormativeDocument> findByAccessLevel(FormativeDocument.AccessLevel accessLevel);

    List<FormativeDocument> findByCreationDateAfter(LocalDateTime date);

    List<FormativeDocument> findByDocumentTypeAndAllowedStagesContaining(
            FormativeDocument.DocumentType documentType, User.LifeStage stage);

    List<FormativeDocument> findByDocumentTypeAndAllowedLocationsContaining(
            FormativeDocument.DocumentType documentType, MissionLocation location);

    @Query("SELECT d FROM FormativeDocument d WHERE d.accessLevel = 'PUBLIC' OR " +
            "d.author = :user OR " +
            "d IN (SELECT d FROM FormativeDocument d JOIN d.allowedUsers u WHERE u = :user) OR " +
            "d IN (SELECT d FROM FormativeDocument d JOIN d.allowedRoles r WHERE r = :#{#user.role}) OR " +
            "(d.accessLevel = 'STAGE_BASED' AND :#{#user.lifeStage} IN (SELECT ls FROM FormativeDocument d JOIN d.allowedStages ls WHERE d = d)) OR " +
            "(d.accessLevel = 'LOCATION_BASED' AND :#{#user.missionLocation} IN (SELECT ml FROM FormativeDocument d JOIN d.allowedLocations ml WHERE d = d))")
    List<FormativeDocument> findAccessibleDocumentsForUser(@Param("user") User user);

    List<FormativeDocument> findByKeywordsContainingIgnoreCase(String keyword);

    @Query("SELECT d FROM FormativeDocument d WHERE d NOT IN " +
            "(SELECT p.document FROM DocumentReadingProgress p WHERE p.user = :user AND p.completed = true) " +
            "AND (d.accessLevel = 'PUBLIC' OR " +
            "d.author = :user OR " +
            "d IN (SELECT d FROM FormativeDocument d JOIN d.allowedUsers u WHERE u = :user) OR " +
            "d IN (SELECT d FROM FormativeDocument d JOIN d.allowedRoles r WHERE r = :#{#user.role}) OR " +
            "(d.accessLevel = 'STAGE_BASED' AND :#{#user.lifeStage} IN (SELECT ls FROM FormativeDocument d JOIN d.allowedStages ls WHERE d = d)) OR " +
            "(d.accessLevel = 'LOCATION_BASED' AND :#{#user.missionLocation} IN (SELECT ml FROM FormativeDocument d JOIN d.allowedLocations ml WHERE d = d)))")
    List<FormativeDocument> findUnreadDocumentsForUser(@Param("user") User user);

    long countByDocumentType(FormativeDocument.DocumentType documentType);

    // SaaS multi-tenancy support
    long countByTenantId(Long tenantId);

    @Query("SELECT COALESCE(SUM(LENGTH(d.attachmentData)), 0) FROM FormativeDocument d WHERE d.tenantId = :tenantId")
    long sumAttachmentSizeByTenantId(@Param("tenantId") Long tenantId);
}