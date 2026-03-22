package ru.tbank.practicum.service;

import com.sun.nio.sctp.IllegalReceiveException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.entity.RadiatorRule;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.LogStatus;
import ru.tbank.practicum.repositories.RadiatorRuleRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RadiatorRuleService {

    private final RadiatorRuleRepository radiatorRuleRepository;
    private final LogService logService;

    @Transactional(readOnly = true)
    public RadiatorRule getById(Long id) {
        return radiatorRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RadiatorRule with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<RadiatorRule> getAll() {
        return radiatorRuleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RadiatorRule> getActiveRulesByRadiatorId(Long radiatorId) {
        return radiatorRuleRepository.findByRadiatorIdAndEnabledTrue(radiatorId);
    }

    public RadiatorRule save(RadiatorRule radiatorRule) {
        validateRule(
                radiatorRule.getMinOutsideTemp(),
                radiatorRule.getMaxOutsideTemp(),
                radiatorRule.getTargetRadiatorTemp()
        );

        RadiatorRule saved = radiatorRuleRepository.save(radiatorRule);

        log.info("RadiatorRule created: id={}, radiatorId={}", saved.getId(), saved.getRadiator().getId());

        logService.createLog(
                DeviceType.RADIATOR,
                saved.getRadiator().getId(),
                LogStatus.SUCCESS,
                EventSource.SYSTEM,
                "CREATE_RADIATOR_RULE",
                "Radiator rule was created successfully"
        );

        return saved;
    }

    public RadiatorRule updateRadiatorRule(Long id, BigDecimal min, BigDecimal max, BigDecimal target, Boolean enabled) {
        validateRule(min, max, target);

        RadiatorRule radiatorRule = getById(id);

        radiatorRule.setMinOutsideTemp(min);
        radiatorRule.setMaxOutsideTemp(max);
        radiatorRule.setTargetRadiatorTemp(target);
        radiatorRule.setEnabled(enabled);

        log.info("RadiatorRule {} updated", id);

        logService.createLog(
                DeviceType.RADIATOR,
                radiatorRule.getRadiator().getId(),
                LogStatus.SUCCESS,
                EventSource.SYSTEM,
                "UPDATE_RADIATOR_RULE",
                "Radiator rule was updated successfully"
        );

        return radiatorRule;
    }

    private void validateRule(BigDecimal min, BigDecimal max, BigDecimal target) {
        if (min == null || max == null || target == null) {
            throw new IllegalArgumentException("Rule values cannot be null");
        }

        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min cannot be greater than max");
        }
    }
}

