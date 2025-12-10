package com.gestaoformativa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "follow_up_meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpMeeting extends TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Formador responsável pelo acompanhamento
    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    // Usuário sendo acompanhado
    @ManyToOne
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime scheduledDate;

    private LocalDateTime actualDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingType meetingType;

    // Conteúdo/notas do acompanhamento
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    // Objetivos do acompanhamento
    @Lob
    @Column(columnDefinition = "TEXT")
    private String objectives;

    // Pontos discutidos
    @Lob
    @Column(columnDefinition = "TEXT")
    private String discussionPoints;

    // Compromissos assumidos
    @Lob
    @Column(columnDefinition = "TEXT")
    private String commitments;

    // Próximos passos
    @Lob
    @Column(columnDefinition = "TEXT")
    private String nextSteps;

    // Observações do formador
    @Lob
    @Column(columnDefinition = "TEXT")
    private String mentorNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisibilityLevel visibility = VisibilityLevel.PRIVATE;

    // Usuários com quem o acompanhamento foi compartilhado
    @ManyToMany
    @JoinTable(
            name = "follow_up_shared_with",
            joinColumns = @JoinColumn(name = "follow_up_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> sharedWith = new HashSet<>();

    // Roles com quem o acompanhamento foi compartilhado
    @ManyToMany
    @JoinTable(
            name = "follow_up_shared_roles",
            joinColumns = @JoinColumn(name = "follow_up_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> sharedRoles = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Enums
    public enum MeetingStatus {
        SCHEDULED("Agendado"),
        COMPLETED("Realizado"),
        CANCELLED("Cancelado"),
        RESCHEDULED("Remarcado");

        private final String displayName;

        MeetingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MeetingType {
        SPIRITUAL_DIRECTION("Direção Espiritual"),
        VOCATIONAL_ACCOMPANIMENT("Acompanhamento Vocacional"),
        FORMATIVE_EVALUATION("Avaliação Formativa"),
        PERSONAL_DEVELOPMENT("Desenvolvimento Pessoal"),
        COMMUNITY_INTEGRATION("Integração Comunitária"),
        ACADEMIC_FOLLOW_UP("Acompanhamento Acadêmico"),
        PASTORAL_SUPERVISION("Supervisão Pastoral"),
        OTHER("Outro");

        private final String displayName;

        MeetingType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum VisibilityLevel {
        PRIVATE("Privado - Apenas o Formador"),
        SHARED_SPECIFIC("Compartilhado - Usuários Específicos"),
        SHARED_ROLE("Compartilhado - Por Função"),
        COORDINATION("Coordenação - Visível para Coordenadores");

        private final String displayName;

        VisibilityLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Métodos auxiliares
    public boolean canBeAccessedBy(User user) {
        // O formador sempre tem acesso
        if (user.equals(mentor)) {
            return true;
        }

        // O acompanhado tem acesso ao próprio acompanhamento
        if (user.equals(mentee)) {
            return true;
        }

        // Verificar visibilidade
        switch (visibility) {
            case PRIVATE:
                return false;

            case SHARED_SPECIFIC:
                return sharedWith != null && sharedWith.contains(user);

            case SHARED_ROLE:
                return sharedRoles != null && user.getRole() != null &&
                        sharedRoles.contains(user.getRole());

            case COORDINATION:
                // Verificar se é coordenador de algum local
                return user.getMissionLocation() != null &&
                        user.equals(user.getMissionLocation().getCoordinator());

            default:
                return false;
        }
    }

    public boolean canBeEditedBy(User user) {
        // Apenas o formador pode editar
        return user.equals(mentor);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}