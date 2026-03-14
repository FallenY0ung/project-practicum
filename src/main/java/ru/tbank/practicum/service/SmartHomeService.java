package ru.tbank.practicum.service;

import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.tbank.practicum.dto.CurtainsSchedule;
import ru.tbank.practicum.model.BlindsState;

@Service
public class SmartHomeService {

    private static final Logger log = LoggerFactory.getLogger(SmartHomeService.class);

    private int radiatorTargetTemp = 22;
    private BlindsState blindsState = BlindsState.CLOSED;
    private CurtainsSchedule schedule = new CurtainsSchedule(LocalTime.of(8, 0), LocalTime.of(21, 0), false);

    public int getRadiatorTargetTemp() {
        return radiatorTargetTemp;
    }

    public BlindsState getBlindsState() {
        return blindsState;
    }

    public CurtainsSchedule getSchedule() {
        return schedule;
    }

    public void setRadiatorTargetTemp(int newTemp) {
        int old = radiatorTargetTemp;
        radiatorTargetTemp = newTemp;
        log.info("Radiator target temp changed: {} -> {}", old, newTemp);
    }

    public void setBlindsState(BlindsState newState) {
        BlindsState old = blindsState;
        blindsState = newState;
        log.info("Blinds state changed: {} -> {}", old, newState);
    }

    public void setSchedule(CurtainsSchedule newSchedule) {
        CurtainsSchedule old = schedule;
        schedule = newSchedule;
        log.info("Curtains schedule changed: {} -> {}", old, newSchedule);
    }
}
