package com.gestaoformativa.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReadingProgressDTO {
    private Long id;
    private Long userId;
    private Long documentId;
    private String documentTitle;
    private Integer progressPercentage;
    private Boolean completed;
    private LocalDateTime firstViewDate;
    private LocalDateTime lastViewDate;
    private LocalDateTime completedDate;
    private String userNotes;
}