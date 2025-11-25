package com.gestaoformativa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;
    private String name;
    private String description;
    private Boolean canManageUsers;
    private Boolean canManageRoles;
    private Boolean canManageStages;
    private Boolean canManageDocuments;

    // Podemos adicionar um campo que indica quantos usuários possuem este papel
    private Integer userCount;
}