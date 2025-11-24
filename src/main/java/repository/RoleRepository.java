package repository;

import model.FormativeDocument;
import model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Buscar por nome
    Optional<Role> findByName(String name);

    // Buscar papéis que podem gerenciar documentos
    List<Role> findByCanManageDocumentsTrue();

    // Buscar papéis com acesso a um documento específico
    @Query("SELECT r FROM Role r JOIN r.accessibleDocuments d WHERE d = :document")
    List<Role> findRolesWithAccessToDocument(FormativeDocument document);

    // Buscar papéis com acesso a documentos por tipo
    @Query("SELECT DISTINCT r FROM Role r JOIN r.accessibleDocuments d WHERE d.documentType = :documentType")
    List<Role> findRolesWithAccessToDocumentType(FormativeDocument.DocumentType documentType);

    // Buscar papéis que podem gerenciar usuários e documentos
    List<Role> findByCanManageUsersIsTrueAndCanManageDocumentsIsTrue();
}