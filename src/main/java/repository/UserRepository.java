package repository;

import model.FormativeDocument;
import model.MissionLocation;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

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
}