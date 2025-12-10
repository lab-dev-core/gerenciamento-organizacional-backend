package com.gestaoformativa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String name;
    private String role;
    private Long tenantId;
    private String tenantName;

    public JwtResponse(String token, Long id, String username, String name, String role, Long tenantId, String tenantName) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
    }
}
