package repository;

import model.FormativeStage;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FormativeStageRepository extends JpaRepository<FormativeStage, Long> {

    // Buscar por usuário
    List<FormativeStage> findByUser(User user);

    // Buscar por nome de estágio
    List<FormativeStage> findByNameContainingIgnoreCase(String name);

    // Buscar estágios ativos (sem data de término)
    List<FormativeStage> findByEndDateIsNull();

    // Buscar estágios que começaram depois de uma data
    List<FormativeStage> findByStartDateAfter(LocalDate date);

    // Buscar estágios que terminaram antes de uma data
    List<FormativeStage> findByEndDateBefore(LocalDate date);

    // Buscar estágios que estão ativos em uma data específica
    @Query("SELECT fs FROM FormativeStage fs WHERE fs.startDate <= :date AND (fs.endDate IS NULL OR fs.endDate >= :date)")
    List<FormativeStage> findStagesActiveAtDate(LocalDate date);

    // Contar estágios por usuário
    long countByUser(User user);

    // Buscar estágios com duração maior que um número de meses
    @Query("SELECT fs FROM FormativeStage fs WHERE FUNCTION('DATEDIFF', 'MONTH', fs.startDate, COALESCE(fs.endDate, CURRENT_DATE)) > :months")
    List<FormativeStage> findStagesLongerThan(int months);

    // Buscar estágios recentemente iniciados
    List<FormativeStage> findTop5ByOrderByStartDateDesc();

    // Buscar estágios recentemente concluídos
    List<FormativeStage> findTop5ByEndDateIsNotNullOrderByEndDateDesc();
}