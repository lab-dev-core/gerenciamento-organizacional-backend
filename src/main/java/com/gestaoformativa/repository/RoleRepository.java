package com.gestaoformativa.repository;

import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    List<Role> findByCanManageDocumentsTrue();

    @Query("SELECT r FROM Role r JOIN r.accessibleDocuments d WHERE d = :document")
    List<Role> findRolesWithAccessToDocument(FormativeDocument document);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.accessibleDocuments d WHERE d.documentType = :documentType")
    List<Role> findRolesWithAccessToDocumentType(FormativeDocument.DocumentType documentType);

    List<Role> findByCanManageUsersIsTrueAndCanManageDocumentsIsTrue();
}