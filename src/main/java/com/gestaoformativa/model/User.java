package com.gestaoformativa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends TenantAware implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isEnabled = true;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isAccountNonExpired = true;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isAccountNonLocked = true;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isCredentialsNonExpired = true;

    @ManyToOne
    @JoinColumn(name = "mission_location_id")
    private MissionLocation missionLocation;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    private int age;

    private String phone;

//    @Lob
//    @Basic(fetch = FetchType.LAZY)
//    @Column(name = "profile_picture", columnDefinition = "BYTEA")
//    private byte[] profilePicture;

    private String education;

    private String email;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private User mentor;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LifeStage lifeStage;

    @Column(nullable = false)
    private Integer communityYears;

    private Integer communityMonths;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<FormativeStage> formativeStages;

    @OneToMany(mappedBy = "author")
    private List<FormativeDocument> authoredDocuments;

    @ManyToMany(mappedBy = "allowedUsers")
    private List<FormativeDocument> accessibleDocuments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DocumentReadingProgress> documentProgress;

    // Acompanhamentos onde este usuário é o formador
    @OneToMany(mappedBy = "mentor")
    private List<FollowUpMeeting> mentoringMeetings;

    // Acompanhamentos onde este usuário é o formando
    @OneToMany(mappedBy = "mentee")
    private List<FollowUpMeeting> menteeMeetings;

    // Método auxiliar para obter todos os acompanhamentos ativos como formador
    public List<FollowUpMeeting> getActiveMentoringMeetings() {
        if (mentoringMeetings == null) {
            return Collections.emptyList();
        }

        return mentoringMeetings.stream()
                .filter(meeting -> meeting.getStatus() == FollowUpMeeting.MeetingStatus.SCHEDULED)
                .sorted(Comparator.comparing(FollowUpMeeting::getScheduledDate))
                .collect(Collectors.toList());
    }

    // Método auxiliar para obter todos os acompanhamentos como formando
    public List<FollowUpMeeting> getMyMenteeMeetings() {
        if (menteeMeetings == null) {
            return Collections.emptyList();
        }

        return menteeMeetings.stream()
                .sorted(Comparator.comparing(FollowUpMeeting::getScheduledDate).reversed())
                .collect(Collectors.toList());
    }

    // Método para verificar se é formador de alguém
    public boolean isMentorOf(User user) {
        if (mentoringMeetings == null || mentoringMeetings.isEmpty()) {
            return false;
        }

        return mentoringMeetings.stream()
                .anyMatch(meeting -> meeting.getMentee().equals(user));
    }

    public enum LifeStage {
        DISCIPLESHIP_IN_MISSION("Discipulado em Missão"),
        DISCIPLESHIP("Discipulado"),
        CONSECRATED_PERMANENT("Consagrado Permanente"),
        VOCATIONAL("Vocacional"),
        ASPIRANCY("Aspirante"),
        MISSION_ASSISTANT("Auxiliar de Missão");

        private final String displayName;

        LifeStage(String displayName) {
            this.displayName = displayName;
        }

        @JsonValue  // ← Esta anotação faz o Jackson usar este valor na serialização
        public String getDisplayName() {
            return displayName;
        }

        @JsonCreator
        public static LifeStage from(String raw) {
            if (raw == null) return null;
            String v = raw.trim().toUpperCase();

            switch (v) {
                // PT-BR → EN
                case "ASPIRANTE":
                    return ASPIRANCY;
                case "DISCIPULADO":
                    return DISCIPLESHIP;
                case "DISCIPULADO_EM_MISSAO":
                    return DISCIPLESHIP_IN_MISSION;
                case "CONSAGRADO_PERMANENTE":
                    return CONSECRATED_PERMANENT;
                case "VOCACIONAL":
                    return VOCATIONAL;
                case "AUXILIAR_DE_MISSAO":
                    return MISSION_ASSISTANT;

                // EN (oficiais)
                case "ASPIRANCY":
                    return ASPIRANCY;
                case "DISCIPLESHIP":
                    return DISCIPLESHIP;
                case "DISCIPLESHIP_IN_MISSION":
                    return DISCIPLESHIP_IN_MISSION;
                case "CONSECRATED_PERMANENT":
                    return CONSECRATED_PERMANENT;
                case "VOCATIONAL":
                    return VOCATIONAL;
                case "MISSION_ASSISTANT":
                    return MISSION_ASSISTANT;

                default:
                    throw new IllegalArgumentException("LifeStage inválido: " + raw);
            }
        }

//        private final String description;
//
//        LifeStage(String description) {
//            this.description = description;
//        }
//
//        public String getDescription() {
//            return description;
//        }
    }

    public String getFullLocation() {
        if (missionLocation != null) {
            return missionLocation.getName() + " (" + missionLocation.getCity() + " - " + missionLocation.getState() + ")";
        } else {
            return city + " - " + state;
        }
    }

    public String getFormattedCommunityTime() {
        StringBuilder time = new StringBuilder();
        if (communityYears != null && communityYears > 0) {
            time.append(communityYears).append(" year(s)");
        }

        if (communityMonths != null && communityMonths > 0) {
            if (time.length() > 0) {
                time.append(" and ");
            }
            time.append(communityMonths).append(" month(s)");
        }

        return time.toString();
    }

    public boolean hasPermission(String permissionType) {
        if (role == null) return false;

        switch (permissionType.toLowerCase()) {
            case "users":
                return role.getCanManageUsers();
            case "roles":
                return role.getCanManageRoles();
            case "stages":
                return role.getCanManageStages();
            case "documents":
                // Adicionando permissão para gerenciar documentos
                return role.getCanManageDocuments();
            default:
                return false;
        }
    }

    public boolean canAccessDocument(FormativeDocument document) {
        if (this.equals(document.getAuthor())) {
            return true;
        }

        return document.canBeAccessedBy(this);
    }

    public DocumentReadingProgress getDocumentProgress(FormativeDocument document) {
        if (documentProgress == null) {
            return null;
        }

        return documentProgress.stream()
                .filter(progress -> progress.getDocument().equals(document))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role != null && "ADMIN".equalsIgnoreCase(role.getName())) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return Boolean.TRUE.equals(isAccountNonExpired);
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(isAccountNonLocked);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return Boolean.TRUE.equals(isCredentialsNonExpired);
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isEnabled);
    }

}