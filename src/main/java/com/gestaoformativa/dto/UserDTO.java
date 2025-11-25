package com.gestaoformativa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gestaoformativa.model.User;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String name;
    private Long missionLocationId;
    private String missionLocationName;
    private String city;
    private String state;
    private int age;
    private String phone;
    private String education;
    private Long mentorId;
    private String mentorName;
    private Long roleId;
    private String roleName;
    private User.LifeStage lifeStage;
    private Integer communityYears;
    private Integer communityMonths;
    private List<FormativeStageDTO> formativeStages;
    private String email;
    private Boolean isEnabled;
    private String password;

    private String fullLocation;
    private String formattedCommunityTime;
    private Boolean canManageUsers;
    private Boolean canManageRoles;
    private Boolean canManageStages;
    private Boolean canManageDocuments;

    private Boolean hasProfilePicture;
}