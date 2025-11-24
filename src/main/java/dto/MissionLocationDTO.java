package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionLocationDTO {

    private Long id;
    private String name;
    private String description;
    private String city;
    private String state;
    private String country;
    private String address;
    private String postalCode;
    private Long coordinatorId;
    private String coordinatorName;

    // Campos adicionais para informações resumidas
    private String fullAddress;
    private Integer userCount;
}