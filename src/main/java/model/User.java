package model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

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

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] profilePicture;

    private String education;

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

    // Novos relacionamentos para documentos

    // Documentos criados pelo usuário
    @OneToMany(mappedBy = "author")
    private List<FormativeDocument> authoredDocuments;

    // Documentos que o usuário tem acesso direto (pessoais)
    @ManyToMany(mappedBy = "allowedUsers")
    private List<FormativeDocument> accessibleDocuments;

    // Progresso de leitura dos documentos
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DocumentReadingProgress> documentProgress;

    public enum LifeStage {
        MISSION_ASSISTANT("Mission Assistant"),
        VOCATIONAL("Vocational"),
        ASPIRANCY("Aspirancy"),
        DISCIPLESHIP("Discipleship"),
        DISCIPLESHIP_IN_MISSION("Discipleship in Mission"),
        CONSECRATED_PERMANENT("Consecrated (permanent)");

        private final String description;

        LifeStage(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
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

    // Método para verificar se o usuário pode acessar um documento específico
    public boolean canAccessDocument(FormativeDocument document) {
        // Se o usuário é o autor do documento
        if (this.equals(document.getAuthor())) {
            return true;
        }

        // Usar o método de verificação da classe FormativeDocument
        return document.canBeAccessedBy(this);
    }

    // Método para obter o progresso de leitura de um documento específico
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