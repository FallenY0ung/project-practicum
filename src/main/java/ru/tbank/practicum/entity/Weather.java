package ru.tbank.practicum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "weather")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal temp;

    @Column(name = "feels_like", nullable = false, precision = 5, scale = 2)
    private BigDecimal feelsLike;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private Long pressure;

    @Column(nullable = false)
    private Long humidity;

    @Column(name = "wind_speed", nullable = false, precision = 5, scale = 2)
    private BigDecimal windSpeed;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @PrePersist
    public void prePersist() {
        if (recordedAt == null) {
            recordedAt = OffsetDateTime.now();
        }
    }
}