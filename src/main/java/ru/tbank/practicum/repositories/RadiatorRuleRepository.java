package ru.tbank.practicum.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.RadiatorRule;

public interface RadiatorRuleRepository extends JpaRepository<RadiatorRule, Long> {
    List<RadiatorRule> findByRadiatorIdAndEnabledTrue(Long radiatorId);

    List<RadiatorRule> findByRadiatorId(Long radiatorId);
}
