package com.gestaoformativa.dto;

import lombok.Data;

@Data
public class SubscriptionRequestDTO {
    private Long tenantId;
    private Long planId;
}