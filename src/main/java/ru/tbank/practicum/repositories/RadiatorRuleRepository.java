package ru.tbank.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.RadiatorRule;

import java.util.List;

public interface RadiatorRuleRepository extends JpaRepository<RadiatorRule, Long> {
    List<RadiatorRule> findByRadiatorIdAndEnabledTrue(Long radiatorId);
}
