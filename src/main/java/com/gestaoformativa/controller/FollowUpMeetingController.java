package com.gestaoformativa.controller;

import com.gestaoformativa.dto.FollowUpMeetingDTO;
import com.gestaoformativa.model.FollowUpMeeting;
import com.gestaoformativa.model.Role;
import com.gestaoformativa.model.User;
import com.gestaoformativa.service.FollowUpMeetingService;
import com.gestaoformativa.service.RoleService;
import com.gestaoformativa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/follow-up")
@Tag(name = "Acompanhamentos", description = "Gerenciamento de reuniões de acompanhamento formativo")
@SecurityRequirement(name = "bearer-jwt")
public class FollowUpMeetingController {

    @Autowired
    private FollowUpMeetingService followUpService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Operation(summary = "Listar acompanhamentos acessíveis", description = "Retorna todos os acompanhamentos que o usuário pode acessar")
    @ApiResponse(responseCode = "200", description = "Acompanhamentos listados com sucesso")
    @GetMapping
    public ResponseEntity<List<FollowUpMeetingDTO>> getAccessibleFollowUpMeetings(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FollowUpMeeting> meetings = followUpService.getAccessibleFollowUpMeetings(currentUser);

        List<FollowUpMeetingDTO> dtos = meetings.stream()
                .map(meeting -> convertToDTO(meeting, currentUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Meus acompanhamentos", description = "Retorna acompanhamentos criados pelo formador logado")
    @ApiResponse(responseCode = "200", description = "Acompanhamentos listados com sucesso")
    @GetMapping("/my-meetings")
    public ResponseEntity<List<FollowUpMeetingDTO>> getMyFollowUpMeetings(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FollowUpMeeting> meetings = followUpService.getFollowUpMeetingsByMentor(currentUser);

        List<FollowUpMeetingDTO> dtos = meetings.stream()
                .map(meeting -> convertToDTO(meeting, currentUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Acompanhamentos por mentorado", description = "Retorna acompanhamentos de um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acompanhamentos listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Mentorado não encontrado")
    })
    @GetMapping("/mentee/{menteeId}")
    public ResponseEntity<List<FollowUpMeetingDTO>> getFollowUpMeetingsByMentee(
            @Parameter(description = "ID do mentorado") @PathVariable Long menteeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            User mentee = userService.getUserById(menteeId);
            List<FollowUpMeeting> meetings = followUpService.getFollowUpMeetingsByMentee(mentee);

            List<FollowUpMeetingDTO> dtos = meetings.stream()
                    .filter(meeting -> meeting.canBeAccessedBy(currentUser))
                    .map(meeting -> convertToDTO(meeting, currentUser))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Acompanhamentos por status", description = "Retorna acompanhamentos filtrando por status")
    @ApiResponse(responseCode = "200", description = "Acompanhamentos listados com sucesso")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FollowUpMeetingDTO>> getFollowUpMeetingsByStatus(
            @Parameter(description = "Status do acompanhamento") @PathVariable FollowUpMeeting.MeetingStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FollowUpMeeting> meetings = followUpService.getFollowUpMeetingsByStatus(currentUser, status);

        List<FollowUpMeetingDTO> dtos = meetings.stream()
                .map(meeting -> convertToDTO(meeting, currentUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Próximos acompanhamentos", description = "Retorna acompanhamentos agendados para os próximos dias")
    @ApiResponse(responseCode = "200", description = "Acompanhamentos listados com sucesso")
    @GetMapping("/upcoming")
    public ResponseEntity<List<FollowUpMeetingDTO>> getUpcomingMeetings(
            @Parameter(description = "Número de dias a partir de hoje") @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FollowUpMeeting> meetings = followUpService.getUpcomingFollowUpMeetings(currentUser, days);

        List<FollowUpMeetingDTO> dtos = meetings.stream()
                .map(meeting -> convertToDTO(meeting, currentUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Acompanhamentos por período", description = "Retorna acompanhamentos em um intervalo de datas")
    @ApiResponse(responseCode = "200", description = "Acompanhamentos listados com sucesso")
    @GetMapping("/date-range")
    public ResponseEntity<List<FollowUpMeetingDTO>> getFollowUpMeetingsByDateRange(
            @Parameter(description = "Data de início") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Data de fim") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FollowUpMeeting> meetings = followUpService.getFollowUpMeetingsByDateRange(
                currentUser, startDate, endDate);

        List<FollowUpMeetingDTO> dtos = meetings.stream()
                .map(meeting -> convertToDTO(meeting, currentUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Obter acompanhamento por ID", description = "Retorna um acompanhamento específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acompanhamento encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Acompanhamento não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FollowUpMeetingDTO> getFollowUpMeetingById(
            @Parameter(description = "ID do acompanhamento") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FollowUpMeeting meeting = followUpService.getFollowUpMeetingById(id);

            if (!meeting.canBeAccessedBy(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(convertToDTO(meeting, currentUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Criar acompanhamento", description = "Cria um novo registro de acompanhamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Acompanhamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<FollowUpMeetingDTO> createFollowUpMeeting(
            @Valid @RequestBody FollowUpMeetingDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FollowUpMeeting meeting = convertToEntity(dto, currentUser);
            FollowUpMeeting savedMeeting = followUpService.createFollowUpMeeting(meeting, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertToDTO(savedMeeting, currentUser));
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Atualizar acompanhamento", description = "Atualiza um acompanhamento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acompanhamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Acompanhamento não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<FollowUpMeetingDTO> updateFollowUpMeeting(
            @Parameter(description = "ID do acompanhamento") @PathVariable Long id,
            @Valid @RequestBody FollowUpMeetingDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FollowUpMeeting meeting = convertToEntity(dto, currentUser);
            FollowUpMeeting updatedMeeting = followUpService.updateFollowUpMeeting(id, meeting, currentUser);

            return ResponseEntity.ok(convertToDTO(updatedMeeting, currentUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Excluir acompanhamento", description = "Exclui um acompanhamento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Acompanhamento excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Acompanhamento não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFollowUpMeeting(
            @Parameter(description = "ID do acompanhamento") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            followUpService.deleteFollowUpMeeting(id, currentUser);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Compartilhar com usuário", description = "Compartilha um acompanhamento com um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acompanhamento compartilhado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Acompanhamento ou usuário não encontrado")
    })
    @PostMapping("/{id}/share/user/{userId}")
    public ResponseEntity<FollowUpMeetingDTO> shareWithUser(
            @Parameter(description = "ID do acompanhamento") @PathVariable Long id,
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FollowUpMeeting meeting = followUpService.shareWithUser(id, userId, currentUser);
            return ResponseEntity.ok(convertToDTO(meeting, currentUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Compartilhar com role", description = "Compartilha um acompanhamento com todos os usuários de uma role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acompanhamento compartilhado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Acompanhamento ou role não encontrado")
    })
    @PostMapping("/{id}/share/role/{roleId}")
    public ResponseEntity<FollowUpMeetingDTO> shareWithRole(
            @Parameter(description = "ID do acompanhamento") @PathVariable Long id,
            @Parameter(description = "ID da role") @PathVariable Long roleId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FollowUpMeeting meeting = followUpService.shareWithRole(id, roleId, currentUser);
            return ResponseEntity.ok(convertToDTO(meeting, currentUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Remover compartilhamento com usuário", description = "Remove o compartilhamento de um acompanhamento com um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compartilhamento removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Acompanhamento ou usuário não encontrado")
    })
    @DeleteMapping("/{id}/share/user/{userId}")
    public ResponseEntity<FollowUpMeetingDTO> removeShareWithUser(
            @Parameter(description = "ID do acompanhamento") @PathVariable Long id,
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FollowUpMeeting meeting = followUpService.removeShareWithUser(id, userId, currentUser);
            return ResponseEntity.ok(convertToDTO(meeting, currentUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Marcar como realizado", description = "Marca um acompanhamento como realizado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acompanhamento marcado como realizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Acompanhamento não encontrado")
    })
    @PutMapping("/{id}/complete")
    public ResponseEntity<FollowUpMeetingDTO> markAsCompleted(
            @Parameter(description = "ID do acompanhamento") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FollowUpMeeting meeting = followUpService.markAsCompleted(id, currentUser);
            return ResponseEntity.ok(convertToDTO(meeting, currentUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Cancelar acompanhamento", description = "Cancela um acompanhamento agendado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acompanhamento cancelado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Acompanhamento não encontrado")
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<FollowUpMeetingDTO> cancelMeeting(
            @Parameter(description = "ID do acompanhamento") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FollowUpMeeting meeting = followUpService.cancelMeeting(id, currentUser);
            return ResponseEntity.ok(convertToDTO(meeting, currentUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Obter estatísticas", description = "Retorna estatísticas de acompanhamentos")
    @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso")
    @GetMapping("/statistics")
    public ResponseEntity<FollowUpMeetingService.FollowUpStatistics> getStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        FollowUpMeetingService.FollowUpStatistics stats = followUpService.getStatistics(currentUser);

        return ResponseEntity.ok(stats);
    }

    // Métodos auxiliares de conversão
    private FollowUpMeetingDTO convertToDTO(FollowUpMeeting meeting, User currentUser) {
        FollowUpMeetingDTO dto = new FollowUpMeetingDTO();

        dto.setId(meeting.getId());
        dto.setMentorId(meeting.getMentor().getId());
        dto.setMentorName(meeting.getMentor().getName());
        dto.setMenteeId(meeting.getMentee().getId());
        dto.setMenteeName(meeting.getMentee().getName());
        dto.setTitle(meeting.getTitle());
        dto.setScheduledDate(meeting.getScheduledDate());
        dto.setActualDate(meeting.getActualDate());
        dto.setStatus(meeting.getStatus());
        dto.setMeetingType(meeting.getMeetingType());
        dto.setContent(meeting.getContent());
        dto.setObjectives(meeting.getObjectives());
        dto.setDiscussionPoints(meeting.getDiscussionPoints());
        dto.setCommitments(meeting.getCommitments());
        dto.setNextSteps(meeting.getNextSteps());
        dto.setMentorNotes(meeting.getMentorNotes());
        dto.setVisibility(meeting.getVisibility());
        dto.setCreatedAt(meeting.getCreatedAt());
        dto.setUpdatedAt(meeting.getUpdatedAt());

        // Campos adicionais
        dto.setMenteeLifeStage(meeting.getMentee().getLifeStage().getDisplayName());
        dto.setMenteeLocation(meeting.getMentee().getFullLocation());
        dto.setCanEdit(meeting.canBeEditedBy(currentUser));
        dto.setIsUpcoming(meeting.getStatus() == FollowUpMeeting.MeetingStatus.SCHEDULED &&
                meeting.getScheduledDate().isAfter(LocalDateTime.now()));

        // IDs de usuários compartilhados
        if (meeting.getSharedWith() != null) {
            dto.setSharedWithUserIds(meeting.getSharedWith().stream()
                    .map(User::getId)
                    .collect(Collectors.toSet()));
        }

        // IDs de roles compartilhadas
        if (meeting.getSharedRoles() != null) {
            dto.setSharedRoleIds(meeting.getSharedRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    private FollowUpMeeting convertToEntity(FollowUpMeetingDTO dto, User currentUser) {
        FollowUpMeeting meeting = new FollowUpMeeting();

        // Mentor é sempre o usuário atual ao criar
        meeting.setMentor(currentUser);

        // Buscar mentee
        User mentee = userService.getUserById(dto.getMenteeId());
        meeting.setMentee(mentee);

        meeting.setTitle(dto.getTitle());
        meeting.setScheduledDate(dto.getScheduledDate());
        meeting.setActualDate(dto.getActualDate());
        meeting.setStatus(dto.getStatus());
        meeting.setMeetingType(dto.getMeetingType());
        meeting.setContent(dto.getContent());
        meeting.setObjectives(dto.getObjectives());
        meeting.setDiscussionPoints(dto.getDiscussionPoints());
        meeting.setCommitments(dto.getCommitments());
        meeting.setNextSteps(dto.getNextSteps());
        meeting.setMentorNotes(dto.getMentorNotes());
        meeting.setVisibility(dto.getVisibility());

        // Processar usuários compartilhados
        if (dto.getSharedWithUserIds() != null && !dto.getSharedWithUserIds().isEmpty()) {
            Set<User> sharedUsers = new HashSet<>();
            for (Long userId : dto.getSharedWithUserIds()) {
                try {
                    User user = userService.getUserById(userId);
                    sharedUsers.add(user);
                } catch (EntityNotFoundException e) {
                    // Log e ignora usuários inválidos
                }
            }
            meeting.setSharedWith(sharedUsers);
        }

        // Processar roles compartilhadas
        if (dto.getSharedRoleIds() != null && !dto.getSharedRoleIds().isEmpty()) {
            Set<Role> sharedRoles = new HashSet<>();
            for (Long roleId : dto.getSharedRoleIds()) {
                try {
                    Role role = roleService.getRoleById(roleId);
                    sharedRoles.add(role);
                } catch (EntityNotFoundException e) {
                    // Log e ignora roles inválidas
                }
            }
            meeting.setSharedRoles(sharedRoles);
        }

        return meeting;
    }
}