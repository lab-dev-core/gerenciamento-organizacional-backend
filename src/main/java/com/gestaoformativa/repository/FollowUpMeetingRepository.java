package com.gestaoformativa.repository;

import com.gestaoformativa.model.FollowUpMeeting;
import com.gestaoformativa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FollowUpMeetingRepository extends JpaRepository<FollowUpMeeting, Long> {

    // Buscar todos os acompanhamentos de um formador
    List<FollowUpMeeting> findByMentorOrderByScheduledDateDesc(User mentor);

    // Buscar todos os acompanhamentos de um usuário específico
    List<FollowUpMeeting> findByMenteeOrderByScheduledDateDesc(User mentee);

    // Buscar acompanhamentos por status
    List<FollowUpMeeting> findByMentorAndStatus(User mentor, FollowUpMeeting.MeetingStatus status);

    // Buscar acompanhamentos por tipo
    List<FollowUpMeeting> findByMentorAndMeetingType(User mentor, FollowUpMeeting.MeetingType meetingType);

    // Buscar acompanhamentos em um período
    @Query("SELECT f FROM FollowUpMeeting f WHERE f.mentor = :mentor " +
            "AND f.scheduledDate BETWEEN :startDate AND :endDate " +
            "ORDER BY f.scheduledDate DESC")
    List<FollowUpMeeting> findByMentorAndDateRange(
            @Param("mentor") User mentor,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Buscar acompanhamentos próximos (nos próximos X dias)
    @Query("SELECT f FROM FollowUpMeeting f WHERE f.mentor = :mentor " +
            "AND f.status = 'SCHEDULED' " +
            "AND f.scheduledDate BETWEEN :now AND :futureDate " +
            "ORDER BY f.scheduledDate ASC")
    List<FollowUpMeeting> findUpcomingMeetings(
            @Param("mentor") User mentor,
            @Param("now") LocalDateTime now,
            @Param("futureDate") LocalDateTime futureDate
    );

    // Buscar acompanhamentos compartilhados com um usuário
    @Query("SELECT f FROM FollowUpMeeting f " +
            "WHERE :user MEMBER OF f.sharedWith " +
            "ORDER BY f.scheduledDate DESC")
    List<FollowUpMeeting> findSharedWithUser(@Param("user") User user);

    // Buscar todos acompanhamentos acessíveis por um usuário
    @Query("SELECT DISTINCT f FROM FollowUpMeeting f " +
            "LEFT JOIN f.sharedWith sw " +
            "LEFT JOIN f.sharedRoles sr " +
            "WHERE f.mentor = :user " +
            "OR f.mentee = :user " +
            "OR sw = :user " +
            "OR (sr = :role AND f.visibility = 'SHARED_ROLE') " +
            "OR (f.visibility = 'COORDINATION' AND :user IN " +
            "    (SELECT ml.coordinator FROM MissionLocation ml)) " +
            "ORDER BY f.scheduledDate DESC")
    List<FollowUpMeeting> findAccessibleByUser(
            @Param("user") User user,
            @Param("role") com.gestaoformativa.model.Role role
    );

    // Contar acompanhamentos por formador
    Long countByMentor(User mentor);

    // Contar acompanhamentos por status
    Long countByMentorAndStatus(User mentor, FollowUpMeeting.MeetingStatus status);

    // Buscar acompanhamentos entre um formador e um usuário específico
    @Query("SELECT f FROM FollowUpMeeting f " +
            "WHERE f.mentor = :mentor AND f.mentee = :mentee " +
            "ORDER BY f.scheduledDate DESC")
    List<FollowUpMeeting> findByMentorAndMentee(
            @Param("mentor") User mentor,
            @Param("mentee") User mentee
    );
}