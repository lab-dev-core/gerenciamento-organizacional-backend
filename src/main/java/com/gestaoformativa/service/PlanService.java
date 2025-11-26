package com.gestaoformativa.service;

import com.gestaoformativa.model.Plan;
import com.gestaoformativa.repository.PlanRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<Plan> findAllActive() {
        return planRepository.findByActiveTrue();
    }

    public Plan findById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));
    }
}
