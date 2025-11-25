package com.gestaoformativa.repository;

import com.gestaoformativa.model.DocumentReadingProgress;
import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentReadingProgressRepository extends JpaRepository<DocumentReadingProgress, Long> {

    Optional<DocumentReadingProgress> findByUserAndDocument(User user, FormativeDocument document);

    List<DocumentReadingProgress> findByUser(User user);

    List<DocumentReadingProgress> findByDocument(FormativeDocument document);

    List<DocumentReadingProgress> findByUserAndCompletedTrue(User user);

    List<DocumentReadingProgress> findByUserAndCompletedFalse(User user);

    @Query("SELECT AVG(p.progressPercentage) FROM DocumentReadingProgress p WHERE p.document = :document")
    Double getAverageProgressForDocument(@Param("document") FormativeDocument document);

    List<DocumentReadingProgress> findByUserAndLastViewDateAfter(User user, LocalDateTime date);

    @Query("SELECT p.document FROM DocumentReadingProgress p WHERE p.user = :user ORDER BY p.lastViewDate DESC")
    List<FormativeDocument> findRecentlyViewedDocuments(@Param("user") User user);

}