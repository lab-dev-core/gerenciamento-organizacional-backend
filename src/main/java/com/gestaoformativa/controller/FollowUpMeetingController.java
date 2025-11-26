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

@RestController
@RequestMapping("/api/follow-up")
public class FollowUpMeetingController {

    @Autowired
    private FollowUpMeetingService followUpService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    // Obter todos os acompanhamentos acessíveis pelo usuário logado
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

    // Obter acompanhamentos criados pelo formador logado
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

    // Obter acompanhamentos de um usuário específico
    @GetMapping("/mentee/{menteeId}")
    public ResponseEntity<List<FollowUpMeetingDTO>> getFollowUpMeetingsByMentee(
            @PathVariable Long menteeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            User mentee = userService.getUserById(menteeId);
            List<FollowUpMeeting> meetings = followUpService.getFollowUpMeetingsByMentee(mentee);

            // Filtrar apenas os acompanhamentos que o usuário pode acessar
            List<FollowUpMeetingDTO> dtos = meetings.stream()
                    .filter(meeting -> meeting.canBeAccessedBy(currentUser))
                    .map(meeting -> convertToDTO(meeting, currentUser))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obter acompanhamentos por status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FollowUpMeetingDTO>> getFollowUpMeetingsByStatus(
            @PathVariable FollowUpMeeting.MeetingStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FollowUpMeeting> meetings = followUpService.getFollowUpMeetingsByStatus(currentUser, status);

        List<FollowUpMeetingDTO> dtos = meetings.stream()
                .map(meeting -> convertToDTO(meeting, currentUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Obter acompanhamentos próximos
    @GetMapping("/upcoming")
    public ResponseEntity<List<FollowUpMeetingDTO>> getUpcomingMeetings(
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FollowUpMeeting> meetings = followUpService.getUpcomingFollowUpMeetings(currentUser, days);

        List<FollowUpMeetingDTO> dtos = meetings.stream()
                .map(meeting -> convertToDTO(meeting, currentUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Obter acompanhamentos em um período
    @GetMapping("/date-range")
    public ResponseEntity<List<FollowUpMeetingDTO>> getFollowUpMeetingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FollowUpMeeting> meetings = followUpService.getFollowUpMeetingsByDateRange(
                currentUser, startDate, endDate);

        List<FollowUpMeetingDTO> dtos = meetings.stream()
                .map(meeting -> convertToDTO(meeting, currentUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Obter um acompanhamento específico
    @GetMapping("/{id}")
    public ResponseEntity<FollowUpMeetingDTO> getFollowUpMeetingById(
            @PathVariable Long id,
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

    // Criar novo acompanhamento
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

    // Atualizar acompanhamento
    @PutMapping("/{id}")
    public ResponseEntity<FollowUpMeetingDTO> updateFollowUpMeeting(
            @PathVariable Long id,
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

    // Excluir acompanhamento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFollowUpMeeting(
            @PathVariable Long id,
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

    // Compartilhar acompanhamento com usuário
    @PostMapping("/{id}/share/user/{userId}")
    public ResponseEntity<FollowUpMeetingDTO> shareWithUser(
            @PathVariable Long id,
            @PathVariable Long userId,
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

    // Compartilhar acompanhamento com role
    @PostMapping("/{id}/share/role/{roleId}")
    public ResponseEntity<FollowUpMeetingDTO> shareWithRole(
            @PathVariable Long id,
            @PathVariable Long roleId,
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

    // Remover compartilhamento
    @DeleteMapping("/{id}/share/user/{userId}")
    public ResponseEntity<FollowUpMeetingDTO> removeShareWithUser(
            @PathVariable Long id,
            @PathVariable Long userId,
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

    // Marcar como realizado
    @PutMapping("/{id}/complete")
    public ResponseEntity<FollowUpMeetingDTO> markAsCompleted(
            @PathVariable Long id,
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

    // Cancelar acompanhamento
    @PutMapping("/{id}/cancel")
    public ResponseEntity<FollowUpMeetingDTO> cancelMeeting(
            @PathVariable Long id,
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

    // Obter estatísticas
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