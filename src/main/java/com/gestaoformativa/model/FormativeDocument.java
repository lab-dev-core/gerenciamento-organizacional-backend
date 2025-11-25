package com.gestaoformativa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "formative_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormativeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime creationDate;

    private LocalDateTime lastModifiedDate;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel accessLevel;

    // Tipo de documento: PERSONAL, STAGE, LOCATION, GENERAL
    public enum DocumentType {
        PERSONAL("Personal Document"),
        STAGE_SPECIFIC("Stage-Specific Document"),
        LOCATION_SPECIFIC("Location-Specific Document"),
        GENERAL("General Community Document");

        private final String description;

        DocumentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Níveis de acesso
    public enum AccessLevel {
        PRIVATE("Private - Author Only"),
        RESTRICTED("Restricted - Specific Users/Roles"),
        STAGE_BASED("Stage-Based - Users in specific stages"),
        LOCATION_BASED("Location-Based - Users in specific locations"),
        PUBLIC("Public - All community members");

        private final String description;

        AccessLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @ElementCollection(targetClass = User.LifeStage.class)
    @CollectionTable(name = "document_allowed_stages",
            joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "life_stage")
    @Enumerated(EnumType.STRING)
    private Set<User.LifeStage> allowedStages = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "document_allowed_locations",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private Set<MissionLocation> allowedLocations;

    @ManyToMany
    @JoinTable(
            name = "document_allowed_users",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> allowedUsers;

    // Para documentos com acesso restrito a funções específicas
    @ManyToMany
    @JoinTable(
            name = "document_allowed_roles",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> allowedRoles;

    @Lob
    @Column(name = "attachment_data")
    @Basic(fetch = FetchType.LAZY)
    private byte[] attachmentData;

    private String attachmentName;

    private String attachmentType;

    // Metadata
    private String keywords;

    // Verificação se o documento pode ser acessado por um usuário específico
    public boolean canBeAccessedBy(User user) {
        // Se o usuário é o autor, sempre tem acesso
        if (user.equals(author)) {
            return true;
        }

        // Verificar com base no nível de acesso
        switch (accessLevel) {
            case PRIVATE:
                // Somente o autor tem acesso (já verificado acima)
                return false;

            case PUBLIC:
                // Todos os membros da comunidade têm acesso
                return true;

            case RESTRICTED:
                // Verificar se o usuário está na lista de usuários permitidos
                if (allowedUsers != null && allowedUsers.contains(user)) {
                    return true;
                }

                // Verificar se a função do usuário está na lista de funções permitidas
                if (allowedRoles != null && user.getRole() != null && allowedRoles.contains(user.getRole())) {
                    return true;
                }

                return false;

            case STAGE_BASED:
                // Verificar se a etapa de vida do usuário está na lista de etapas permitidas
                return allowedStages != null && allowedStages.contains(user.getLifeStage());

            case LOCATION_BASED:
                // Verificar se o local do usuário está na lista de locais permitidos
                return allowedLocations != null && user.getMissionLocation() != null &&
                        allowedLocations.contains(user.getMissionLocation());

            default:
                return false;
        }
    }

    // Pre-persist para definir a data de criação
    @PrePersist
    protected void onCreate() {
        creationDate = LocalDateTime.now();
        lastModifiedDate = LocalDateTime.now();
    }

    // Pre-update para atualizar a data de modificação
    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}