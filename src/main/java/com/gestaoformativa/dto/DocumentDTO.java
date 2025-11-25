package com.gestaoformativa.dto;

import lombok.Data;
import com.gestaoformativa.model.FormativeDocument;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class DocumentDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime creationDate;
    private LocalDateTime lastModifiedDate;
    private Long authorId;
    private String authorName;
    private FormativeDocument.DocumentType documentType;
    private FormativeDocument.AccessLevel accessLevel;
    private Set<String> allowedStages;
    private Set<Long> allowedLocationIds;
    private Set<Long> allowedUserIds;
    private Set<Long> allowedRoleIds;
    private String attachmentName;
    private String attachmentType;
    private String keywords;
    private Integer readingProgressPercentage;
    private Boolean completed;
}