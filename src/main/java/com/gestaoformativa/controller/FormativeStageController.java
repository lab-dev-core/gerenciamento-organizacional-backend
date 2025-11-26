package com.gestaoformativa.controller;

import com.gestaoformativa.dto.FormativeStageDTO;
import com.gestaoformativa.model.FormativeStage;
import com.gestaoformativa.model.User;
import com.gestaoformativa.service.FormativeStageService;
import com.gestaoformativa.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/stages")
@Tag(name = "Etapas Formativas", description = "Gerenciamento de etapas formativas dos usuários")
@SecurityRequirement(name = "bearer-jwt")
public class FormativeStageController {

    @Autowired
    private FormativeStageService stageService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Listar etapas", description = "Retorna todas as etapas formativas (requer permissão)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapas listadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar etapas")
    })
    @GetMapping
    public ResponseEntity<List<FormativeStageDTO>> getAllStages(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getAllStages();

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    @Operation(summary = "Obter etapa por ID", description = "Retorna uma etapa formativa específica pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapa encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Etapa não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FormativeStageDTO> getStageById(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FormativeStage stage = stageService.getStageById(id);

            if (!stage.getUser().equals(currentUser) && !currentUser.hasPermission("stages")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(convertToDTO(stage));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Etapas por usuário", description = "Retorna etapas formativas de um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapas listadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FormativeStageDTO>> getStagesByUser(@PathVariable Long userId,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

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

    @Operation(summary = "Etapas ativas", description = "Retorna etapas formativas ativas (não concluídas)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapas ativas listadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar etapas")
    })
    @GetMapping("/active")
    public ResponseEntity<List<FormativeStageDTO>> getActiveStages(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getActiveStages();

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    @Operation(summary = "Etapas ativas em data", description = "Retorna etapas formativas ativas em uma data específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapas listadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar etapas")
    })
    @GetMapping("/active-at")
    public ResponseEntity<List<FormativeStageDTO>> getStagesActiveAtDate(
            @Parameter(description = "Data para verificação") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getStagesActiveAtDate(date);

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    @Operation(summary = "Criar etapa", description = "Cria uma nova etapa formativa para um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Etapa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping("/user/{userId}")
    public ResponseEntity<FormativeStageDTO> createStage(@PathVariable Long userId,
                                                         @Valid @RequestBody FormativeStageDTO stageDTO,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

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

    @Operation(summary = "Atualizar etapa", description = "Atualiza uma etapa formativa existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Etapa não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<FormativeStageDTO> updateStage(@PathVariable Long id,
                                                         @Valid @RequestBody FormativeStageDTO stageDTO,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FormativeStage existingStage = stageService.getStageById(id);

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

    @Operation(summary = "Concluir etapa", description = "Marca uma etapa formativa como concluída")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapa concluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Etapa já concluída ou dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Etapa não encontrada")
    })
    @PutMapping("/{id}/complete")
    public ResponseEntity<FormativeStageDTO> completeStage(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FormativeStage existingStage = stageService.getStageById(id);

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

    @Operation(summary = "Excluir etapa", description = "Exclui uma etapa formativa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Etapa excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Etapa não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStage(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FormativeStage existingStage = stageService.getStageById(id);

            if (!existingStage.getUser().equals(currentUser) && !currentUser.hasPermission("stages")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            stageService.deleteStage(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Etapas longas", description = "Retorna etapas com duração maior que X meses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapas listadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar etapas")
    })
    @GetMapping("/longer-than/{months}")
    public ResponseEntity<List<FormativeStageDTO>> getStagesLongerThan(@PathVariable int months,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getStagesLongerThan(months);

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    @Operation(summary = "Etapas recentemente iniciadas", description = "Retorna etapas que começaram recentemente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapas listadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar etapas")
    })
    @GetMapping("/recently-started")
    public ResponseEntity<List<FormativeStageDTO>> getRecentlyStartedStages(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("stages")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FormativeStage> stages = stageService.getRecentlyStartedStages();

        List<FormativeStageDTO> stageDTOs = stages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stageDTOs);
    }

    @Operation(summary = "Etapas recentemente concluídas", description = "Retorna etapas que foram concluídas recentemente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Etapas listadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar etapas")
    })
    @GetMapping("/recently-completed")
    public ResponseEntity<List<FormativeStageDTO>> getRecentlyCompletedStages(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

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

        dto.setIsActive(stage.isActive());

        return dto;
    }

    private FormativeStage convertToEntity(FormativeStageDTO dto) {
        FormativeStage stage = new FormativeStage();

        stage.setName(dto.getName());
        stage.setStartDate(dto.getStartDate());
        stage.setEndDate(dto.getEndDate());

        return stage;
    }
}