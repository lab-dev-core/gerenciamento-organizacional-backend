package controller;

import dto.FormativeStageDTO;
import model.FormativeStage;
import model.User;
import service.FormativeStageService;
import service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stages")
public class FormativeStageController {

    @Autowired
    private FormativeStageService stageService;

    @Autowired
    private UserService userService;

    // Obter todas as etapas formativas
    @GetMapping
    public ResponseEntity<List<FormativeStageDTO>> getAllStages(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar etapas
        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getAllStages();

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    // Obter uma etapa formativa específica
    @GetMapping("/{id}")
    public ResponseEntity<FormativeStageDTO> getStageById(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FormativeStage stage = stageService.getStageById(id);

            // Verificar se o usuário é o dono da etapa ou tem permissão para gerenciar etapas
            if (!stage.getUser().equals(currentUser) && !currentUser.hasPermission("stages")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(convertToDTO(stage));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obter etapas formativas por usuário
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FormativeStageDTO>> getStagesByUser(@PathVariable Long userId,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário está consultando suas próprias etapas ou tem permissão
        if (!currentUser.getId().equals(userId) && !currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<FormativeStage> stages = stageService.getStagesByUser(userId);

            List<FormativeStageDTO> stageDTOs = stages.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(stageDTOs);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obter etapas formativas ativas (não concluídas)
    @GetMapping("/active")
    public ResponseEntity<List<FormativeStageDTO>> getActiveStages(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar etapas
        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getActiveStages();

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    // Obter etapas formativas ativas em uma data específica
    @GetMapping("/active-at")
    public ResponseEntity<List<FormativeStageDTO>> getStagesActiveAtDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar etapas
        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getStagesActiveAtDate(date);

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    // Criar uma nova etapa formativa
    @PostMapping("/user/{userId}")
    public ResponseEntity<FormativeStageDTO> createStage(@PathVariable Long userId,
                                                         @Valid @RequestBody FormativeStageDTO stageDTO,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário está criando para si mesmo ou tem permissão
        if (!currentUser.getId().equals(userId) && !currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            FormativeStage stage = convertToEntity(stageDTO);
            FormativeStage createdStage = stageService.createStage(stage, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(createdStage));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Atualizar uma etapa formativa existente
    @PutMapping("/{id}")
    public ResponseEntity<FormativeStageDTO> updateStage(@PathVariable Long id,
                                                         @Valid @RequestBody FormativeStageDTO stageDTO,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FormativeStage existingStage = stageService.getStageById(id);

            // Verificar se o usuário é o dono da etapa ou tem permissão
            if (!existingStage.getUser().equals(currentUser) && !currentUser.hasPermission("stages")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            FormativeStage stage = convertToEntity(stageDTO);
            FormativeStage updatedStage = stageService.updateStage(id, stage);
            return ResponseEntity.ok(convertToDTO(updatedStage));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Concluir uma etapa formativa
    @PutMapping("/{id}/complete")
    public ResponseEntity<FormativeStageDTO> completeStage(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FormativeStage existingStage = stageService.getStageById(id);

            // Verificar se o usuário é o dono da etapa ou tem permissão
            if (!existingStage.getUser().equals(currentUser) && !currentUser.hasPermission("stages")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            FormativeStage completedStage = stageService.completeStage(id);
            return ResponseEntity.ok(convertToDTO(completedStage));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Excluir uma etapa formativa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStage(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FormativeStage existingStage = stageService.getStageById(id);

            // Verificar se o usuário é o dono da etapa ou tem permissão
            if (!existingStage.getUser().equals(currentUser) && !currentUser.hasPermission("stages")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            stageService.deleteStage(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obter etapas formativas longas (com duração maior que um número de meses)
    @GetMapping("/longer-than/{months}")
    public ResponseEntity<List<FormativeStageDTO>> getStagesLongerThan(@PathVariable int months,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar etapas
        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getStagesLongerThan(months);

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    // Obter etapas formativas recentemente iniciadas
    @GetMapping("/recently-started")
    public ResponseEntity<List<FormativeStageDTO>> getRecentlyStartedStages(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar etapas
        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getRecentlyStartedStages();

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    // Obter etapas formativas recentemente concluídas
    @GetMapping("/recently-completed")
    public ResponseEntity<List<FormativeStageDTO>> getRecentlyCompletedStages(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar etapas
        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getRecentlyCompletedStages();

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    // Métodos auxiliares
    private FormativeStageDTO convertToDTO(FormativeStage stage) {
        FormativeStageDTO dto = new FormativeStageDTO();
        dto.setId(stage.getId());
        dto.setName(stage.getName());
        dto.setStartDate(stage.getStartDate());
        dto.setEndDate(stage.getEndDate());
        dto.setDurationMonths(stage.getDurationMonths());

        if (stage.getUser() != null) {
            dto.setUserId(stage.getUser().getId());
            dto.setUserName(stage.getUser().getName());
        }

        // Informações adicionais
        dto.setIsActive(stage.isActive());

        return dto;
    }

    private FormativeStage convertToEntity(FormativeStageDTO dto) {
        FormativeStage stage = new FormativeStage();

        // Não definimos o ID ao criar uma nova entidade
        // Se estamos atualizando, o ID será definido pelo método de serviço

        stage.setName(dto.getName());
        stage.setStartDate(dto.getStartDate());
        stage.setEndDate(dto.getEndDate());

        // O usuário será definido pelo serviço

        return stage;
    }
}