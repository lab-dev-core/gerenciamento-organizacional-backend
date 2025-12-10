package com.gestaoformativa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_reading_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReadingProgress extends TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private FormativeDocument document;

    // Percentual de progresso (0-100)
    private Integer progressPercentage = 0;

    // Indica se o documento foi concluído
    private Boolean completed = false;

    // Data da primeira visualização
    private LocalDateTime firstViewDate;

    // Data da última visualização
    private LocalDateTime lastViewDate;

    // Data de conclusão da leitura
    private LocalDateTime completedDate;

    // Anotações do usuário sobre o documento
    @Lob
    @Column(columnDefinition = "TEXT")
    private String userNotes;

    // Pre-persist para definir a data da primeira visualização
    @PrePersist
    protected void onCreate() {
        firstViewDate = LocalDateTime.now();
        lastViewDate = LocalDateTime.now();
    }

    // Pre-update para atualizar a data da última visualização
    @PreUpdate
    protected void onUpdate() {
        lastViewDate = LocalDateTime.now();

        // Se o progresso atingiu 100%, marcar como concluído
        if (progressPercentage != null && progressPercentage >= 100 && !completed) {
            completed = true;
            completedDate = LocalDateTime.now();
        }
    }
}