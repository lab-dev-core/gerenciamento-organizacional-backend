
package com.gestaoformativa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role extends TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private Boolean canManageUsers = false;
    private Boolean canManageRoles = false;
    private Boolean canManageStages = false;
    private Boolean canManageDocuments = false;

    @OneToMany(mappedBy = "role")
    private List<User> users;

    @ManyToMany(mappedBy = "allowedRoles")
    private List<FormativeDocument> accessibleDocuments;
}