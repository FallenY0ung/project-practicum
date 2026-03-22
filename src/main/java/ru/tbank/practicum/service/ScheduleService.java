package ru.tbank.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.entity.Schedule;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.LogStatus;
import ru.tbank.practicum.repositories.ScheduleRepository;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final LogService logService;

    @Transactional(readOnly = true)
    public Schedule getById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAll() {
        return scheduleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Schedule> getActiveByBlindsId(Long blindsId) {
        return scheduleRepository.findByBlindsIdAndEnabledTrue(blindsId);
    }

    public Schedule save(Schedule schedule) {
        validateSchedule(schedule.getOpenAt(), schedule.getCloseAt(), schedule.getEnabled());

        Schedule saved = scheduleRepository.save(schedule);

        log.info("Schedule created: id={}, blindsId={}", saved.getId(), saved.getBlinds().getId());

        logService.createLog(
                DeviceType.BLINDS,
                saved.getBlinds().getId(),
                LogStatus.SUCCESS,
                EventSource.SYSTEM,
                "CREATE_SCHEDULE",
                "Schedule was created successfully"
        );

        return saved;
    }

    public Schedule updateSchedule(Long id, LocalTime openAt, LocalTime closeAt, Boolean enabled) {
        validateSchedule(openAt, closeAt, enabled);

        Schedule schedule = getById(id);
        schedule.setOpenAt(openAt);
        schedule.setCloseAt(closeAt);
        schedule.setEnabled(enabled);

        log.info("Schedule {} updated", id);

        logService.createLog(
                DeviceType.BLINDS,
                schedule.getBlinds().getId(),
                LogStatus.SUCCESS,
                EventSource.SYSTEM,
                "UPDATE_SCHEDULE",
                "Schedule was updated successfully"
        );

        return schedule;
    }

    private void validateSchedule(LocalTime openAt, LocalTime closeAt, Boolean enabled) {
        if (openAt == null || closeAt == null) {
            throw new IllegalArgumentException("Open and close time cannot be null");
        }
        if (!openAt.isBefore(closeAt)) {
            throw new IllegalArgumentException("Open time must be before close time");
        }
        if (enabled == null) {
            throw new IllegalArgumentException("Enabled flag cannot be null");
        }
    }
}
