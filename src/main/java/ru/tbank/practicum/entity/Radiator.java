package ru.tbank.practicum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table (name = "radiator")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Radiator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal temp;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline;

    @Column(name = "is_broken", nullable = false)
    private Boolean isBroken;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
