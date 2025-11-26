package com.gestaoformativa.dto;

import lombok.Data;

@Data
public class TenantRegistrationDTO {
    private String name;
    private String subdomain;
    private String adminEmail;
    private String adminPassword;
}
