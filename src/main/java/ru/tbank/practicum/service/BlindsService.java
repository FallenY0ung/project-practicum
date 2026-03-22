package ru.tbank.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.enums.*;
import ru.tbank.practicum.repositories.BlindsRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BlindsService {

    private final BlindsRepository blindsRepository;
    private final DeviceEventService deviceEventService;
    private final LogService logService;

    @Transactional(readOnly = true)
    public Blinds getById(Long id) {
        return blindsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blinds with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Blinds> getAll() {
        return blindsRepository.findAll();
    }

    public Blinds save(Blinds blinds) {
        Blinds saved = blindsRepository.save(blinds);

        log.info("Blinds created: id={}, state={}", saved.getId(), saved.getState());

        logService.createLog(
                DeviceType.BLINDS,
                saved.getId(),
                LogStatus.SUCCESS,
                EventSource.SYSTEM,
                "CREATE_BLINDS",
                "Blinds were created successfully"
        );

        return saved;
    }

    public Blinds updateState(Long id, BlindsState newState, EventSource source) {
        Blinds blinds = getById(id);
        BlindsState oldState = blinds.getState();

        if (oldState == newState) {
            log.info("Blinds {} state is already {}", id, newState);
            return blinds;
        }

        blinds.setState(newState);

        log.info("Blinds {} state changed: {} -> {}", id, oldState, newState);

        deviceEventService.createEvent(
                DeviceType.BLINDS,
                blinds.getId(),
                mapStateToEventType(newState),
                source,
                "Blinds state changed from " + oldState + " to " + newState
        );

        logService.createLog(
                DeviceType.BLINDS,
                blinds.getId(),
                LogStatus.SUCCESS,
                source,
                "UPDATE_BLINDS_STATE",
                "Blinds state changed from " + oldState + " to " + newState
        );

        return blinds;
    }

    public Blinds changeOnlineStatus(Long id, boolean online, EventSource source) {
        Blinds blinds = getById(id);

        if (Boolean.valueOf(online).equals(blinds.getIsOnline())) {
            log.info("Blinds {} online status is already {}", id, online);
            return blinds;
        }

        blinds.setIsOnline(online);

        log.info("Blinds {} online status changed to {}", id, online);

        deviceEventService.createEvent(
                DeviceType.BLINDS,
                blinds.getId(),
                online ? EventType.BLINDS_ONLINE : EventType.BLINDS_OFFLINE,
                source,
                "Blinds online status changed to " + online
        );

        logService.createLog(
                DeviceType.BLINDS,
                blinds.getId(),
                LogStatus.SUCCESS,
                source,
                "CHANGE_BLINDS_ONLINE_STATUS",
                "Blinds online status changed to " + online
        );

        return blinds;
    }

    public Blinds markAsBroken(Long id, EventSource source) {
        Blinds blinds = getById(id);

        if (Boolean.TRUE.equals(blinds.getIsBroken())) {
            log.info("Blinds {} already marked as broken", id);
            return blinds;
        }

        blinds.setIsBroken(true);

        log.info("Blinds {} marked as broken", id);

        deviceEventService.createEvent(
                DeviceType.BLINDS,
                blinds.getId(),
                EventType.BLINDS_BROKEN,
                source,
                "Blinds marked as broken"
        );

        logService.createLog(
                DeviceType.BLINDS,
                blinds.getId(),
                LogStatus.WARNING,
                source,
                "MARK_BLINDS_BROKEN",
                "Blinds marked as broken"
        );

        return blinds;
    }

    public Blinds restore(Long id, EventSource source) {
        Blinds blinds = getById(id);

        if (Boolean.FALSE.equals(blinds.getIsBroken())) {
            log.info("Blinds {} already restored", id);
            return blinds;
        }

        blinds.setIsBroken(false);

        log.info("Blinds {} restored", id);

        deviceEventService.createEvent(
                DeviceType.BLINDS,
                blinds.getId(),
                EventType.BLINDS_RESTORED,
                source,
                "Blinds restored"
        );

        logService.createLog(
                DeviceType.BLINDS,
                blinds.getId(),
                LogStatus.SUCCESS,
                source,
                "RESTORE_BLINDS",
                "Blinds restored successfully"
        );

        return blinds;
    }

    private EventType mapStateToEventType(BlindsState state) {
        return switch (state) {
            case OPEN -> EventType.BLINDS_OPENED;
            case CLOSED -> EventType.BLINDS_CLOSED;
            case HALF_OPEN -> EventType.BLINDS_HALF_OPENED;
        };
    }
}
