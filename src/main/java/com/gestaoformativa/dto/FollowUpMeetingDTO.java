package com.gestaoformativa.dto;

import com.gestaoformativa.model.FollowUpMeeting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpMeetingDTO {

    private Long id;

    @NotNull(message = "Mentor ID é obrigatório")
    private Long mentorId;

    private String mentorName;

    @NotNull(message = "Mentee ID é obrigatório")
    private Long menteeId;

    private String menteeName;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotNull(message = "Data agendada é obrigatória")
    private LocalDateTime scheduledDate;

    private LocalDateTime actualDate;

    @NotNull(message = "Status é obrigatório")
    private FollowUpMeeting.MeetingStatus status;

    @NotNull(message = "Tipo de encontro é obrigatório")
    private FollowUpMeeting.MeetingType meetingType;

    private String content;

    private String objectives;

    private String discussionPoints;

    private String commitments;

    private String nextSteps;

    private String mentorNotes;

    @NotNull(message = "Nível de visibilidade é obrigatório")
    private FollowUpMeeting.VisibilityLevel visibility;

    private Set<Long> sharedWithUserIds;

    private Set<Long> sharedRoleIds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Campos adicionais para exibição
    private String menteeLifeStage;
    private String menteeLocation;
    private Boolean canEdit;
    private Boolean isUpcoming;
}