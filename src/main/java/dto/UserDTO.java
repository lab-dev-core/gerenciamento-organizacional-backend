package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.User;
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

    // Campos adicionais para informações resumidas
    private String fullLocation;
    private String formattedCommunityTime;
    private Boolean canManageUsers;
    private Boolean canManageRoles;
    private Boolean canManageStages;
    private Boolean canManageDocuments;

    // Não incluímos a senha aqui por questões de segurança
    // Também não incluímos dados binários como imagem de perfil diretamente
    // Para imagem de perfil, um campo booleano indicando presença ou um URL podem ser usados
    private Boolean hasProfilePicture;
}