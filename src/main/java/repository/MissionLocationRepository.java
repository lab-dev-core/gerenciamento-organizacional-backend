package repository;

import model.FormativeDocument;
import model.MissionLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionLocationRepository extends JpaRepository<MissionLocation, Long> {

    // Buscar por nome
    MissionLocation findByName(String name);

    // Buscar por cidade
    List<MissionLocation> findByCity(String city);

    // Buscar por estado
    List<MissionLocation> findByState(String state);

    // Buscar locais com documentos específicos
    @Query("SELECT DISTINCT l FROM MissionLocation l JOIN l.accessibleDocuments d WHERE d = :document")
    List<MissionLocation> findLocationsWithAccessToDocument(FormativeDocument document);

    // Buscar locais com documentos específicos por tipo de documento
    @Query("SELECT DISTINCT l FROM MissionLocation l JOIN l.accessibleDocuments d WHERE d.documentType = :documentType")
    List<MissionLocation> findLocationsWithDocumentType(FormativeDocument.DocumentType documentType);

    // Buscar por país
    List<MissionLocation> findByCountry(String country);

    // Buscar locais sem coordenador atribuído
    List<MissionLocation> findByCoordinatorIsNull();
}