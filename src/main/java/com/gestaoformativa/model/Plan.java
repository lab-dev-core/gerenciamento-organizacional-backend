package com.gestaoformativa.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Data
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer maxUsers = 5;

    @Column(nullable = false)
    private Integer maxDocuments = 100;

    @Column(nullable = false)
    private Long maxStorageMb = 1024L;

    @Column(nullable = false)
    private Boolean active = true;
}
