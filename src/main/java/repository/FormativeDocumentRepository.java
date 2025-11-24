package repository;

import model.FormativeDocument;
import model.MissionLocation;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FormativeDocumentRepository extends JpaRepository<FormativeDocument, Long> {

    // Buscar por título (correspondência parcial)
    List<FormativeDocument> findByTitleContainingIgnoreCase(String title);

    // Buscar por autor
    List<FormativeDocument> findByAuthor(User author);

    // Buscar por tipo de documento
    List<FormativeDocument> findByDocumentType(FormativeDocument.DocumentType documentType);

    // Buscar por nível de acesso
    List<FormativeDocument> findByAccessLevel(FormativeDocument.AccessLevel accessLevel);

    // Buscar documentos criados depois de uma data específica
    List<FormativeDocument> findByCreationDateAfter(LocalDateTime date);

    // Buscar documentos específicos de uma etapa
    List<FormativeDocument> findByDocumentTypeAndAllowedStagesContaining(
            FormativeDocument.DocumentType documentType, User.LifeStage stage);

    // Buscar documentos específicos de um local
    List<FormativeDocument> findByDocumentTypeAndAllowedLocationsContaining(
            FormativeDocument.DocumentType documentType, MissionLocation location);

    // Buscar documentos que um usuário específico pode acessar (por papel)
    @Query("SELECT d FROM FormativeDocument d WHERE d.accessLevel = 'PUBLIC' OR " +
            "d.author = :user OR " +
            "d IN (SELECT d FROM FormativeDocument d JOIN d.allowedUsers u WHERE u = :user) OR " +
            "d IN (SELECT d FROM FormativeDocument d JOIN d.allowedRoles r WHERE r = :#{#user.role}) OR " +
            "(d.accessLevel = 'STAGE_BASED' AND :#{#user.lifeStage} IN (SELECT ls FROM FormativeDocument d JOIN d.allowedStages ls WHERE d = d)) OR " +
            "(d.accessLevel = 'LOCATION_BASED' AND :#{#user.missionLocation} IN (SELECT ml FROM FormativeDocument d JOIN d.allowedLocations ml WHERE d = d))")
    List<FormativeDocument> findAccessibleDocumentsForUser(@Param("user") User user);

    // Buscar documentos por palavra-chave
    List<FormativeDocument> findByKeywordsContainingIgnoreCase(String keyword);

    // Buscar documentos não lidos por um usuário específico
    @Query("SELECT d FROM FormativeDocument d WHERE d NOT IN " +
            "(SELECT p.document FROM DocumentReadingProgress p WHERE p.user = :user AND p.completed = true) " +
            "AND (d.accessLevel = 'PUBLIC' OR " +
            "d.author = :user OR " +
            "d IN (SELECT d FROM FormativeDocument d JOIN d.allowedUsers u WHERE u = :user) OR " +
            "d IN (SELECT d FROM FormativeDocument d JOIN d.allowedRoles r WHERE r = :#{#user.role}) OR " +
            "(d.accessLevel = 'STAGE_BASED' AND :#{#user.lifeStage} IN (SELECT ls FROM FormativeDocument d JOIN d.allowedStages ls WHERE d = d)) OR " +
            "(d.accessLevel = 'LOCATION_BASED' AND :#{#user.missionLocation} IN (SELECT ml FROM FormativeDocument d JOIN d.allowedLocations ml WHERE d = d)))")
    List<FormativeDocument> findUnreadDocumentsForUser(@Param("user") User user);

    // Contar documentos por tipo
    long countByDocumentType(FormativeDocument.DocumentType documentType);
}