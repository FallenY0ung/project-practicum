package ru.tbank.practicum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blinds_id", nullable = false)
    private Blinds blinds;

    @Column(name = "open_at", nullable = false)
    private LocalTime openAt;

    @Column(name = "close_at", nullable = false)
    private LocalTime closeAt;

    @Column(nullable = false)
    private Boolean enabled;
}
