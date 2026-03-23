package ru.tbank.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.entity.Radiator;
import ru.tbank.practicum.entity.RadiatorRule;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.EventType;
import ru.tbank.practicum.enums.LogStatus;
import ru.tbank.practicum.repositories.RadiatorRepository;
import ru.tbank.practicum.repositories.RadiatorRuleRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RadiatorService {

    private final RadiatorRepository radiatorRepository;
    private final DeviceEventService deviceEventService;
    private final LogService logService;
    private final RadiatorRuleRepository radiatorRuleRepository;

    @Transactional(readOnly = true)
    public Radiator getById(Long id) {
        return radiatorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Radiator with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Radiator> getAll() {
        return radiatorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RadiatorRule> getByRadiatorId(Long radiatorId){
        return radiatorRuleRepository.findByRadiatorId(radiatorId);
    }

    public Radiator save(Radiator radiator, EventSource eventSource) {
        Radiator saved = radiatorRepository.save(radiator);

        log.info("Radiator created: id={}, temp={}", saved.getId(), saved.getTemp());

        logService.createLog(
                DeviceType.RADIATOR,
                saved.getId(),
                LogStatus.SUCCESS,
                eventSource,
                "CREATE_RADIATOR",
                "Radiator was created successfully"
        );

        return saved;
    }

    public void deleteById(Long id) {
        Radiator radiator = getById(id);

        if (!radiatorRuleRepository.findByRadiatorIdAndEnabledTrue(id).isEmpty()) {
            throw new IllegalStateException("Cannot delete radiator with existing rules");
        }

        log.info("Deleting radiator {}", id);


        logService.createLog(
                DeviceType.RADIATOR,
                radiator.getId(),
                LogStatus.WARNING,
                EventSource.USER,
                "DELETE_RADIATOR",
                "Radiator was deleted"
        );

        radiatorRepository.delete(radiator);
    }

    public Radiator updateTemperature(Long id, BigDecimal newTemp, EventSource source) {
        if (newTemp == null) {
            throw new IllegalArgumentException("Temperature cannot be null");
        }

        Radiator radiator = getById(id);
        BigDecimal oldTemp = radiator.getTemp();

        if (oldTemp != null && oldTemp.compareTo(newTemp) == 0) {
            log.info("Radiator {} temperature is already {}", id, newTemp);
            return radiator;
        }

        radiator.setTemp(newTemp);

        log.info("Radiator {} temperature changed: {} -> {}", id, oldTemp, newTemp);

        deviceEventService.createEvent(
                DeviceType.RADIATOR,
                radiator.getId(),
                EventType.RADIATOR_TEMPERATURE_SET,
                source,
                "Radiator temperature changed from " + oldTemp + " to " + newTemp
        );

        logService.createLog(
                DeviceType.RADIATOR,
                radiator.getId(),
                LogStatus.SUCCESS,
                source,
                "UPDATE_TEMPERATURE",
                "Radiator temperature changed from " + oldTemp + " to " + newTemp
        );

        return radiator;
    }

    public Radiator markAsBroken(Long id, EventSource source) {
        Radiator radiator = getById(id);

        if (Boolean.TRUE.equals(radiator.getIsBroken())) {
            log.info("Radiator {} already marked as broken", id);
            return radiator;
        }

        radiator.setIsBroken(true);

        log.info("Radiator {} marked as broken", id);

        deviceEventService.createEvent(
                DeviceType.RADIATOR,
                radiator.getId(),
                EventType.RADIATOR_BROKEN,
                source,
                "Radiator marked as broken"
        );

        logService.createLog(
                DeviceType.RADIATOR,
                radiator.getId(),
                LogStatus.WARNING,
                source,
                "MARK_AS_BROKEN",
                "Radiator status changed to broken"
        );

        return radiator;
    }

    public Radiator restore(Long id, EventSource source) {
        Radiator radiator = getById(id);

        if (Boolean.FALSE.equals(radiator.getIsBroken())) {
            log.info("Radiator {} already restored", id);
            return radiator;
        }

        radiator.setIsBroken(false);

        log.info("Radiator {} restored", id);

        deviceEventService.createEvent(
                DeviceType.RADIATOR,
                radiator.getId(),
                EventType.RADIATOR_RESTORED,
                source,
                "Radiator restored"
        );

        logService.createLog(
                DeviceType.RADIATOR,
                radiator.getId(),
                LogStatus.SUCCESS,
                source,
                "RESTORE_RADIATOR",
                "Radiator restored successfully"
        );

        return radiator;
    }

    public Radiator changeOnlineStatus(Long id, boolean online, EventSource source) {
        Radiator radiator = getById(id);

        if (Boolean.valueOf(online).equals(radiator.getIsOnline())) {
            log.info("Radiator {} online status is already {}", id, online);
            return radiator;
        }

        radiator.setIsOnline(online);

        log.info("Radiator {} online status changed to {}", id, online);

        deviceEventService.createEvent(
                DeviceType.RADIATOR,
                radiator.getId(),
                online ? EventType.RADIATOR_ONLINE : EventType.RADIATOR_OFFLINE,
                source,
                "Radiator online status changed to " + online
        );

        logService.createLog(
                DeviceType.RADIATOR,
                radiator.getId(),
                LogStatus.SUCCESS,
                source,
                "CHANGE_ONLINE_STATUS",
                "Radiator online status updated successfully"
        );

        return radiator;
    }

    public void updateFromForm(Long id, Radiator updatedRadiator, EventSource source) {
        Radiator current = getById(id);

        if (current.getTemp().compareTo(updatedRadiator.getTemp()) != 0) {
            updateTemperature(id, updatedRadiator.getTemp(), source);
        }

        if (!current.getIsBroken().equals(updatedRadiator.getIsBroken())) {
            if (Boolean.TRUE.equals(updatedRadiator.getIsBroken())) {
                markAsBroken(id, source);
            } else {
                restore(id, source);
            }
        }

        if (!current.getIsOnline().equals(updatedRadiator.getIsOnline())) {
            changeOnlineStatus(id, updatedRadiator.getIsOnline(), source);
        }
    }


}
