package ru.tbank.practicum.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tbank.practicum.service.SmartHomeService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleScheduler {

    private final SmartHomeService smartHomeService;

    @Scheduled(fixedDelayString = "${blinds.schedule-interval:1m}")
    public void pollSchedules() {
        log.debug("Starting schedule check for blinds");
        try {
            smartHomeService.applySchedulesToAllBlinds();
            log.info("Schedules applied to all blinds");
        } catch (Exception e) {
            log.warn("Schedule check failed", e);
        }
    }
}
