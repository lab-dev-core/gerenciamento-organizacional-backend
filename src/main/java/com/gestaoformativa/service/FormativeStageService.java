package com.gestaoformativa.service;

import com.gestaoformativa.model.FormativeStage;
import com.gestaoformativa.model.User;
import com.gestaoformativa.repository.FormativeStageRepository;
import com.gestaoformativa.repository.UserRepository;
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

    public List<FormativeStage> getAllStages() {
        return stageRepository.findAll();
    }

    public FormativeStage getStageById(Long id) {
        return stageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Etapa formativa não encontrada com id: " + id));
    }

    public List<FormativeStage> getStagesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + userId));

        return stageRepository.findByUser(user);
    }

    public List<FormativeStage> getActiveStages() {
        return stageRepository.findByEndDateIsNull();
    }

    public FormativeStage createStage(FormativeStage stage, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + userId));

        stage.setUser(user);

        if (stage.getDurationMonths() == null && stage.getStartDate() != null && stage.getEndDate() != null) {
            int months = (stage.getEndDate().getYear() - stage.getStartDate().getYear()) * 12 +
                    stage.getEndDate().getMonthValue() - stage.getStartDate().getMonthValue();
            stage.setDurationMonths(months);
        }

        return stageRepository.save(stage);
    }

    public FormativeStage updateStage(Long id, FormativeStage stageDetails) {
        FormativeStage stage = getStageById(id);

        stage.setName(stageDetails.getName());
        stage.setStartDate(stageDetails.getStartDate());
        stage.setEndDate(stageDetails.getEndDate());

        if (stage.getStartDate() != null && stage.getEndDate() != null) {
            int months = (stage.getEndDate().getYear() - stage.getStartDate().getYear()) * 12 +
                    stage.getEndDate().getMonthValue() - stage.getStartDate().getMonthValue();
            stage.setDurationMonths(months);
        } else if (stageDetails.getDurationMonths() != null) {
            stage.setDurationMonths(stageDetails.getDurationMonths());
        }

        if (stageDetails.getUser() != null && stageDetails.getUser().getId() != null) {
            User user = userRepository.findById(stageDetails.getUser().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " +
                            stageDetails.getUser().getId()));
            stage.setUser(user);
        }

        return stageRepository.save(stage);
    }

    public FormativeStage completeStage(Long id) {
        FormativeStage stage = getStageById(id);

        if (stage.getEndDate() != null) {
            throw new IllegalStateException("Esta etapa formativa já foi concluída");
        }

        stage.setEndDate(LocalDate.now());

        int months = (stage.getEndDate().getYear() - stage.getStartDate().getYear()) * 12 +
                stage.getEndDate().getMonthValue() - stage.getStartDate().getMonthValue();
        stage.setDurationMonths(months);

        return stageRepository.save(stage);
    }

    public void deleteStage(Long id) {
        FormativeStage stage = getStageById(id);
        stageRepository.delete(stage);
    }

    public List<FormativeStage> getStagesActiveAtDate(LocalDate date) {
        return stageRepository.findStagesActiveAtDate(date);
    }

    public List<FormativeStage> getStagesLongerThan(int months) {
        return stageRepository.findStagesLongerThan(months);
    }

    public List<FormativeStage> getRecentlyStartedStages() {
        return stageRepository.findTop5ByOrderByStartDateDesc();
    }

    public List<FormativeStage> getRecentlyCompletedStages() {
        return stageRepository.findTop5ByEndDateIsNotNullOrderByEndDateDesc();
    }
}