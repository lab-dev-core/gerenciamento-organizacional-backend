package com.gestaoformativa.service;

import com.gestaoformativa.model.FollowUpMeeting;
import com.gestaoformativa.model.Role;
import com.gestaoformativa.model.User;
import com.gestaoformativa.repository.FollowUpMeetingRepository;
import com.gestaoformativa.repository.RoleRepository;
import com.gestaoformativa.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FollowUpMeetingService {

    @Autowired
    private FollowUpMeetingRepository followUpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    // Criar novo acompanhamento
    @Transactional
    public FollowUpMeeting createFollowUpMeeting(FollowUpMeeting meeting, User currentUser) {
        // Validar que o usuário atual é o mentor
        if (!meeting.getMentor().equals(currentUser)) {
            throw new IllegalArgumentException("Apenas o formador pode criar o acompanhamento");
        }

        // Validar que mentor e mentee existem
        if (!userRepository.existsById(meeting.getMentor().getId())) {
            throw new EntityNotFoundException("Formador não encontrado");
        }

        if (!userRepository.existsById(meeting.getMentee().getId())) {
            throw new EntityNotFoundException("Usuário acompanhado não encontrado");
        }

        // Validar que mentor e mentee são diferentes
        if (meeting.getMentor().equals(meeting.getMentee())) {
            throw new IllegalArgumentException("Formador e usuário acompanhado devem ser diferentes");
        }

        return followUpRepository.save(meeting);
    }

    // Obter acompanhamento por ID
    public FollowUpMeeting getFollowUpMeetingById(Long id) {
        return followUpRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Acompanhamento não encontrado"));
    }

    // Obter acompanhamentos acessíveis por um usuário
    public List<FollowUpMeeting> getAccessibleFollowUpMeetings(User user) {
        return followUpRepository.findAccessibleByUser(user, user.getRole());
    }

    // Obter acompanhamentos criados por um formador
    public List<FollowUpMeeting> getFollowUpMeetingsByMentor(User mentor) {
        return followUpRepository.findByMentorOrderByScheduledDateDesc(mentor);
    }

    // Obter acompanhamentos de um usuário específico
    public List<FollowUpMeeting> getFollowUpMeetingsByMentee(User mentee) {
        return followUpRepository.findByMenteeOrderByScheduledDateDesc(mentee);
    }

    // Obter acompanhamentos por status
    public List<FollowUpMeeting> getFollowUpMeetingsByStatus(User mentor, FollowUpMeeting.MeetingStatus status) {
        return followUpRepository.findByMentorAndStatus(mentor, status);
    }

    // Obter acompanhamentos próximos (próximos 7 dias)
    public List<FollowUpMeeting> getUpcomingFollowUpMeetings(User mentor, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        return followUpRepository.findUpcomingMeetings(mentor, now, futureDate);
    }

    // Obter acompanhamentos em um período
    public List<FollowUpMeeting> getFollowUpMeetingsByDateRange(
            User mentor, LocalDateTime startDate, LocalDateTime endDate) {
        return followUpRepository.findByMentorAndDateRange(mentor, startDate, endDate);
    }

    // Obter acompanhamentos entre um formador e um usuário específico
    public List<FollowUpMeeting> getFollowUpMeetingsBetween(User mentor, User mentee) {
        return followUpRepository.findByMentorAndMentee(mentor, mentee);
    }

    // Atualizar acompanhamento
    @Transactional
    public FollowUpMeeting updateFollowUpMeeting(Long id, FollowUpMeeting meetingDetails, User currentUser) {
        FollowUpMeeting existingMeeting = getFollowUpMeetingById(id);

        // Verificar se o usuário pode editar
        if (!existingMeeting.canBeEditedBy(currentUser)) {
            throw new IllegalArgumentException("Apenas o formador pode editar este acompanhamento");
        }

        // Atualizar campos
        existingMeeting.setTitle(meetingDetails.getTitle());
        existingMeeting.setScheduledDate(meetingDetails.getScheduledDate());
        existingMeeting.setActualDate(meetingDetails.getActualDate());
        existingMeeting.setStatus(meetingDetails.getStatus());
        existingMeeting.setMeetingType(meetingDetails.getMeetingType());
        existingMeeting.setContent(meetingDetails.getContent());
        existingMeeting.setObjectives(meetingDetails.getObjectives());
        existingMeeting.setDiscussionPoints(meetingDetails.getDiscussionPoints());
        existingMeeting.setCommitments(meetingDetails.getCommitments());
        existingMeeting.setNextSteps(meetingDetails.getNextSteps());
        existingMeeting.setMentorNotes(meetingDetails.getMentorNotes());
        existingMeeting.setVisibility(meetingDetails.getVisibility());

        // Não permitir alterar mentor ou mentee
        // Se necessário, criar um novo acompanhamento

        return followUpRepository.save(existingMeeting);
    }

    // Compartilhar acompanhamento com usuário
    @Transactional
    public FollowUpMeeting shareWithUser(Long meetingId, Long userId, User currentUser) {
        FollowUpMeeting meeting = getFollowUpMeetingById(meetingId);

        // Verificar se o usuário pode compartilhar
        if (!meeting.canBeEditedBy(currentUser)) {
            throw new IllegalArgumentException("Apenas o formador pode compartilhar este acompanhamento");
        }

        User userToShare = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (meeting.getSharedWith() == null) {
            meeting.setSharedWith(new HashSet<>());
        }

        meeting.getSharedWith().add(userToShare);

        // Atualizar visibilidade se necessário
        if (meeting.getVisibility() == FollowUpMeeting.VisibilityLevel.PRIVATE) {
            meeting.setVisibility(FollowUpMeeting.VisibilityLevel.SHARED_SPECIFIC);
        }

        return followUpRepository.save(meeting);
    }

    // Compartilhar acompanhamento com role
    @Transactional
    public FollowUpMeeting shareWithRole(Long meetingId, Long roleId, User currentUser) {
        FollowUpMeeting meeting = getFollowUpMeetingById(meetingId);

        // Verificar se o usuário pode compartilhar
        if (!meeting.canBeEditedBy(currentUser)) {
            throw new IllegalArgumentException("Apenas o formador pode compartilhar este acompanhamento");
        }

        Role roleToShare = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Função não encontrada"));

        if (meeting.getSharedRoles() == null) {
            meeting.setSharedRoles(new HashSet<>());
        }

        meeting.getSharedRoles().add(roleToShare);

        // Atualizar visibilidade
        meeting.setVisibility(FollowUpMeeting.VisibilityLevel.SHARED_ROLE);

        return followUpRepository.save(meeting);
    }

    // Remover compartilhamento com usuário
    @Transactional
    public FollowUpMeeting removeShareWithUser(Long meetingId, Long userId, User currentUser) {
        FollowUpMeeting meeting = getFollowUpMeetingById(meetingId);

        if (!meeting.canBeEditedBy(currentUser)) {
            throw new IllegalArgumentException("Apenas o formador pode remover compartilhamentos");
        }

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (meeting.getSharedWith() != null) {
            meeting.getSharedWith().remove(userToRemove);
        }

        return followUpRepository.save(meeting);
    }

    // Excluir acompanhamento
    @Transactional
    public void deleteFollowUpMeeting(Long id, User currentUser) {
        FollowUpMeeting meeting = getFollowUpMeetingById(id);

        if (!meeting.canBeEditedBy(currentUser)) {
            throw new IllegalArgumentException("Apenas o formador pode excluir este acompanhamento");
        }

        followUpRepository.delete(meeting);
    }

    // Marcar acompanhamento como realizado
    @Transactional
    public FollowUpMeeting markAsCompleted(Long id, User currentUser) {
        FollowUpMeeting meeting = getFollowUpMeetingById(id);

        if (!meeting.canBeEditedBy(currentUser)) {
            throw new IllegalArgumentException("Apenas o formador pode marcar como realizado");
        }

        meeting.setStatus(FollowUpMeeting.MeetingStatus.COMPLETED);
        if (meeting.getActualDate() == null) {
            meeting.setActualDate(LocalDateTime.now());
        }

        return followUpRepository.save(meeting);
    }

    // Cancelar acompanhamento
    @Transactional
    public FollowUpMeeting cancelMeeting(Long id, User currentUser) {
        FollowUpMeeting meeting = getFollowUpMeetingById(id);

        if (!meeting.canBeEditedBy(currentUser)) {
            throw new IllegalArgumentException("Apenas o formador pode cancelar");
        }

        meeting.setStatus(FollowUpMeeting.MeetingStatus.CANCELLED);

        return followUpRepository.save(meeting);
    }

    // Obter estatísticas de acompanhamentos
    public FollowUpStatistics getStatistics(User mentor) {
        Long total = followUpRepository.countByMentor(mentor);
        Long scheduled = followUpRepository.countByMentorAndStatus(
                mentor, FollowUpMeeting.MeetingStatus.SCHEDULED);
        Long completed = followUpRepository.countByMentorAndStatus(
                mentor, FollowUpMeeting.MeetingStatus.COMPLETED);
        Long cancelled = followUpRepository.countByMentorAndStatus(
                mentor, FollowUpMeeting.MeetingStatus.CANCELLED);

        return new FollowUpStatistics(total, scheduled, completed, cancelled);
    }

    // Classe interna para estatísticas
    @Data
    @AllArgsConstructor
    public static class FollowUpStatistics {
        private Long total;
        private Long scheduled;
        private Long completed;
        private Long cancelled;
    }
}