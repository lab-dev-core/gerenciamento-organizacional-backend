package service;

import model.FormativeStage;
import model.User;
import repository.FormativeStageRepository;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

@Service
public class FormativeStageService {

    @Autowired
    private FormativeStageRepository stageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Busca todas as etapas formativas
     */
    public List<FormativeStage> getAllStages() {
        return stageRepository.findAll();
    }

    /**
     * Busca uma etapa formativa por ID
     */
    public FormativeStage getStageById(Long id) {
        return stageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Etapa formativa não encontrada com id: " + id));
    }

    /**
     * Busca etapas formativas por usuário
     */
    public List<FormativeStage> getStagesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + userId));

        return stageRepository.findByUser(user);
    }

    /**
     * Busca etapas formativas ativas (sem data de término)
     */
    public List<FormativeStage> getActiveStages() {
        return stageRepository.findByEndDateIsNull();
    }

    /**
     * Cria uma nova etapa formativa
     */
    public FormativeStage createStage(FormativeStage stage, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + userId));

        stage.setUser(user);

        // Calcular a duração se não for fornecida e ambas as datas estiverem presentes
        if (stage.getDurationMonths() == null && stage.getStartDate() != null && stage.getEndDate() != null) {
            int months = (stage.getEndDate().getYear() - stage.getStartDate().getYear()) * 12 +
                    stage.getEndDate().getMonthValue() - stage.getStartDate().getMonthValue();
            stage.setDurationMonths(months);
        }

        return stageRepository.save(stage);
    }

    /**
     * Atualiza uma etapa formativa existente
     */
    public FormativeStage updateStage(Long id, FormativeStage stageDetails) {
        FormativeStage stage = getStageById(id);

        // Atualizar campos básicos
        stage.setName(stageDetails.getName());
        stage.setStartDate(stageDetails.getStartDate());
        stage.setEndDate(stageDetails.getEndDate());

        // Recalcular a duração se ambas as datas estiverem presentes
        if (stage.getStartDate() != null && stage.getEndDate() != null) {
            int months = (stage.getEndDate().getYear() - stage.getStartDate().getYear()) * 12 +
                    stage.getEndDate().getMonthValue() - stage.getStartDate().getMonthValue();
            stage.setDurationMonths(months);
        } else if (stageDetails.getDurationMonths() != null) {
            stage.setDurationMonths(stageDetails.getDurationMonths());
        }

        // Se fornecido, atualizar o usuário associado
        if (stageDetails.getUser() != null && stageDetails.getUser().getId() != null) {
            User user = userRepository.findById(stageDetails.getUser().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " +
                            stageDetails.getUser().getId()));
            stage.setUser(user);
        }

        return stageRepository.save(stage);
    }

    /**
     * Conclui uma etapa formativa (define a data de término como a data atual)
     */
    public FormativeStage completeStage(Long id) {
        FormativeStage stage = getStageById(id);

        // Verificar se a etapa já foi concluída
        if (stage.getEndDate() != null) {
            throw new IllegalStateException("Esta etapa formativa já foi concluída");
        }

        stage.setEndDate(LocalDate.now());

        // Recalcular a duração
        int months = (stage.getEndDate().getYear() - stage.getStartDate().getYear()) * 12 +
                stage.getEndDate().getMonthValue() - stage.getStartDate().getMonthValue();
        stage.setDurationMonths(months);

        return stageRepository.save(stage);
    }

    /**
     * Exclui uma etapa formativa
     */
    public void deleteStage(Long id) {
        FormativeStage stage = getStageById(id);
        stageRepository.delete(stage);
    }

    /**
     * Busca etapas formativas que estão ativas em uma data específica
     */
    public List<FormativeStage> getStagesActiveAtDate(LocalDate date) {
        return stageRepository.findStagesActiveAtDate(date);
    }

    /**
     * Busca etapas formativas com duração maior que um número de meses
     */
    public List<FormativeStage> getStagesLongerThan(int months) {
        return stageRepository.findStagesLongerThan(months);
    }

    /**
     * Busca etapas formativas recentemente iniciadas
     */
    public List<FormativeStage> getRecentlyStartedStages() {
        return stageRepository.findTop5ByOrderByStartDateDesc();
    }

    /**
     * Busca etapas formativas recentemente concluídas
     */
    public List<FormativeStage> getRecentlyCompletedStages() {
        return stageRepository.findTop5ByEndDateIsNotNullOrderByEndDateDesc();
    }
}