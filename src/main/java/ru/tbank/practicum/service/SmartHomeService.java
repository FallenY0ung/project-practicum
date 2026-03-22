package ru.tbank.practicum.service;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.practicum.dto.CurtainsSchedule;
import ru.tbank.practicum.enums.BlindsState;

@Service
@Slf4j
public class SmartHomeService {

    private final AtomicInteger radiatorTargetTemp = new AtomicInteger(22);
    private final AtomicReference<BlindsState> blindsState = new AtomicReference<>(BlindsState.CLOSED);
    private final AtomicReference<CurtainsSchedule> schedule =
            new AtomicReference<>(new CurtainsSchedule(LocalTime.of(8, 0), LocalTime.of(21, 0), false));

    public int getRadiatorTargetTemp() {
        return radiatorTargetTemp.get();
    }

    public BlindsState getBlindsState() {
        return blindsState.get();
    }

    public CurtainsSchedule getSchedule() {
        return schedule.get();
    }

    public void setRadiatorTargetTemp(AtomicInteger newTemp) {
        int old = radiatorTargetTemp.getAndSet(newTemp.get());
        log.info("Radiator target temp changed: {} -> {}", old, newTemp);
    }

    public void setBlindsState(BlindsState newState) {
        BlindsState old = blindsState.getAndSet(newState);
        log.info("Blinds state changed: {} -> {}", old, newState);
    }

    public void setSchedule(CurtainsSchedule newSchedule) {
        CurtainsSchedule old = schedule.getAndSet(newSchedule);
        log.info("Curtains schedule changed: {} -> {}", old, newSchedule);
    }
}
