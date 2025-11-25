package com.gestaoformativa.repository;

import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.MissionLocation;
import com.gestaoformativa.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentSearchRepository extends JpaRepository<FormativeDocument, Long>, JpaSpecificationExecutor<FormativeDocument> {

    @Query("SELECT d FROM FormativeDocument d WHERE " +
            "(:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR d.author = :author) AND " +
            "(:documentType IS NULL OR d.documentType = :documentType) AND " +
            "(:accessLevel IS NULL OR d.accessLevel = :accessLevel) AND " +
            "(:stage IS NULL OR :stage IN (SELECT ls FROM FormativeDocument fd JOIN fd.allowedStages ls WHERE fd = d)) AND " +
            "(:location IS NULL OR :location IN (SELECT ml FROM FormativeDocument fd JOIN fd.allowedLocations ml WHERE fd = d)) AND " +
            "(:fromDate IS NULL OR d.creationDate >= :fromDate) AND " +
            "(:toDate IS NULL OR d.creationDate <= :toDate) AND " +
            "(:keyword IS NULL OR LOWER(d.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<FormativeDocument> searchDocuments(
            @Param("title") String title,
            @Param("author") User author,
            @Param("documentType") FormativeDocument.DocumentType documentType,
            @Param("accessLevel") FormativeDocument.AccessLevel accessLevel,
            @Param("stage") User.LifeStage stage,
            @Param("location") MissionLocation location,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query(value = """
        SELECT DISTINCT * FROM formative_documents d
        WHERE LOWER(d.content::text) LIKE LOWER(CONCAT('%', :text, '%'))
           OR LOWER(d.title) LIKE LOWER(CONCAT('%', :text, '%'))
           OR LOWER(d.keywords) LIKE LOWER(CONCAT('%', :text, '%'))
        """, nativeQuery = true)
    List<FormativeDocument> searchByContent(@Param("text") String text);

    List<FormativeDocument> findTop10ByOrderByLastModifiedDateDesc();

    @Query("SELECT d, COUNT(p) as viewCount FROM FormativeDocument d JOIN DocumentReadingProgress p " +
            "ON p.document = d GROUP BY d ORDER BY viewCount DESC")
    List<FormativeDocument> findMostViewedDocuments(Pageable pageable);

    @Query("SELECT d FROM FormativeDocument d WHERE " +
            "d NOT IN (SELECT p.document FROM DocumentReadingProgress p WHERE p.user = :user) AND " +
            "((d.accessLevel = 'STAGE_BASED' AND :#{#user.lifeStage} IN (SELECT ls FROM FormativeDocument fd JOIN fd.allowedStages ls WHERE fd = d)) OR " +
            "(d.accessLevel = 'LOCATION_BASED' AND :#{#user.missionLocation} IN (SELECT ml FROM FormativeDocument fd JOIN fd.allowedLocations ml WHERE fd = d)) OR " +
            "d.accessLevel = 'PUBLIC')")
    List<FormativeDocument> findRecommendedDocumentsForUser(@Param("user") User user, Pageable pageable);
}