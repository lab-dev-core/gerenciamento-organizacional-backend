package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "formative_stages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormativeStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private Integer durationMonths;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Integer getDurationMonths() {
        if (startDate == null) return null;

        LocalDate end = endDate != null ? endDate : LocalDate.now();
        int months = (end.getYear() - startDate.getYear()) * 12 + end.getMonthValue() - startDate.getMonthValue();

        return months;
    }

    public boolean isActive() {
        return endDate == null;
    }
}