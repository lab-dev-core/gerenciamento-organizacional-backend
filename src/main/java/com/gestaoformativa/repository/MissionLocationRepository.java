package com.gestaoformativa.repository;

import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.MissionLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionLocationRepository extends JpaRepository<MissionLocation, Long> {

    MissionLocation findByName(String name);

    List<MissionLocation> findByCity(String city);

    List<MissionLocation> findByState(String state);

    @Query("SELECT DISTINCT l FROM MissionLocation l JOIN FormativeDocument d ON l MEMBER OF d.allowedLocations WHERE d = :document")
    List<MissionLocation> findLocationsWithAccessToDocument(FormativeDocument document);

    @Query("SELECT DISTINCT l FROM MissionLocation l JOIN FormativeDocument d ON l MEMBER OF d.allowedLocations WHERE d.documentType = :documentType")
    List<MissionLocation> findLocationsWithDocumentType(FormativeDocument.DocumentType documentType);

    List<MissionLocation> findByCountry(String country);

    List<MissionLocation> findByCoordinatorIsNull();
}