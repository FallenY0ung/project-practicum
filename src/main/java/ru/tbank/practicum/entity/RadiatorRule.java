package ru.tbank.practicum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "radiator_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiatorRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "radiator_id", nullable = false)
    private Radiator radiator;

    @Column(name = "min_outside_temp", nullable = false, precision = 5, scale = 2)
    private BigDecimal minOutsideTemp;

    @Column(name = "max_outside_temp", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxOutsideTemp;

    @Column(name = "target_radiator_temp", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetRadiatorTemp;

    @Column(nullable = false)
    private Boolean enabled;
}