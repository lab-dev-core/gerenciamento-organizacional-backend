package com.gestaoformativa.repository;

import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.MissionLocation;
import com.gestaoformativa.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role", "missionLocation"})
    List<User> findAll();

    @EntityGraph(attributePaths = {"role", "missionLocation"})
    Optional<User> findByUsername(String username);

    List<User> findByNameContainingIgnoreCase(String name);

    List<User> findByMissionLocation(MissionLocation location);

    List<User> findByLifeStage(User.LifeStage lifeStage);

    @Query("SELECT DISTINCT u FROM User u JOIN u.authoredDocuments d")
    List<User> findDocumentAuthors();

    @Query("SELECT u FROM User u JOIN u.accessibleDocuments d WHERE d = :document")
    List<User> findUsersWithAccessToDocument(FormativeDocument document);

    @Query("SELECT u FROM User u JOIN u.documentProgress p WHERE p.document = :document AND p.completed = true")
    List<User> findUsersWhoCompletedDocument(FormativeDocument document);

    List<User> findByRole_Name(String roleName);

    @Query("SELECT u FROM User u JOIN u.role r WHERE r.canManageDocuments = true")
    List<User> findUsersWhoCanManageDocuments();

    // SaaS multi-tenancy support
    long countByTenantId(Long tenantId);
}