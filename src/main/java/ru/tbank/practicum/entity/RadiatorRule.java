package ru.tbank.practicum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
