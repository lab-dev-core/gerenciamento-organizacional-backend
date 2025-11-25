package com.gestaoformativa.repository;

import com.gestaoformativa.model.FormativeStage;
import com.gestaoformativa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FormativeStageRepository extends JpaRepository<FormativeStage, Long> {

    List<FormativeStage> findByUser(User user);

    List<FormativeStage> findByNameContainingIgnoreCase(String name);

    List<FormativeStage> findByEndDateIsNull();

    List<FormativeStage> findByStartDateAfter(LocalDate date);

    List<FormativeStage> findByEndDateBefore(LocalDate date);

    @Query("SELECT fs FROM FormativeStage fs WHERE fs.startDate <= :date AND (fs.endDate IS NULL OR fs.endDate >= :date)")
    List<FormativeStage> findStagesActiveAtDate(@Param("date") LocalDate date);

    long countByUser(User user);

    @Query(value = "SELECT * FROM formative_stage fs WHERE TIMESTAMPDIFF(MONTH, fs.start_date, COALESCE(fs.end_date, CURRENT_DATE)) > :months", nativeQuery = true)
    List<FormativeStage> findStagesLongerThan(@Param("months") int months);

    List<FormativeStage> findTop5ByOrderByStartDateDesc();

    List<FormativeStage> findTop5ByEndDateIsNotNullOrderByEndDateDesc();
}