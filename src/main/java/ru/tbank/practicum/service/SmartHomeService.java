package ru.tbank.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.Radiator;
import ru.tbank.practicum.entity.RadiatorRule;
import ru.tbank.practicum.entity.Schedule;
import ru.tbank.practicum.entity.Weather;
import ru.tbank.practicum.enums.BlindsState;
import ru.tbank.practicum.enums.EventSource;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SmartHomeService {

    private final WeatherService weatherService;
    private final RadiatorService radiatorService;
    private final RadiatorRuleService radiatorRuleService;
    private final BlindsService blindsService;
    private final ScheduleService scheduleService;

    @Transactional(readOnly = true)
    public Weather getLatestWeather() {
        return weatherService.getLatest();
    }

    public void applyWeatherRulesToRadiator(Long radiatorId) {
        Radiator radiator = radiatorService.getById(radiatorId);

        if (Boolean.TRUE.equals(radiator.getIsBroken())) {
            log.info("Skipping radiator {}: device is broken", radiatorId);
            return;
        }

        if (Boolean.FALSE.equals(radiator.getIsOnline())) {
            log.info("Skipping radiator {}: device is offline", radiatorId);
            return;
        }

        Weather weather = weatherService.getLatest();
        BigDecimal outsideTemp = weather.getTemp();

        List<RadiatorRule> rules = radiatorRuleService.getActiveRulesByRadiatorId(radiatorId);

        Optional<RadiatorRule> matchedRule = findMatchingRule(outsideTemp, rules);

        if (matchedRule.isEmpty()) {
            log.info("No matching radiator rule found for radiator {} and outside temp {}", radiatorId, outsideTemp);
            return;
        }

        RadiatorRule rule = matchedRule.get();

        log.info(
                "Applying rule {} to radiator {}: outsideTemp={}, targetTemp={}",
                rule.getId(),
                radiatorId,
                outsideTemp,
                rule.getTargetRadiatorTemp()
        );

        radiatorService.updateTemperature(
                radiatorId,
                rule.getTargetRadiatorTemp(),
                EventSource.WEATHER_RULE
        );
    }

    public void applyWeatherRulesToAllRadiators() {
        List<Radiator> radiators = radiatorService.getAll();

        for (Radiator radiator : radiators) {
            applyWeatherRulesToRadiator(radiator.getId());
        }
    }

    public void applyScheduleToBlinds(Long blindsId) {
        Blinds blinds = blindsService.getById(blindsId);

        if (Boolean.TRUE.equals(blinds.getIsBroken())) {
            log.info("Skipping blinds {}: device is broken", blindsId);
            return;
        }

        if (Boolean.FALSE.equals(blinds.getIsOnline())) {
            log.info("Skipping blinds {}: device is offline", blindsId);
            return;
        }

        LocalTime now = LocalTime.now();
        List<Schedule> schedules = scheduleService.getActiveByBlindsId(blindsId);

        boolean shouldBeOpen = schedules.stream()
                .anyMatch(schedule -> isInsideSchedule(now, schedule));

        BlindsState targetState = shouldBeOpen ? BlindsState.OPEN : BlindsState.CLOSED;

        log.info("Applying schedule to blinds {}: now={}, targetState={}", blindsId, now, targetState);

        blindsService.updateState(blindsId, targetState, EventSource.SCHEDULE);
    }

    public void applySchedulesToAllBlinds() {
        List<Blinds> blindsList = blindsService.getAll();

        for (Blinds blinds : blindsList) {
            applyScheduleToBlinds(blinds.getId());
        }
    }

    public void syncAllDevices() {
        log.info("Starting full smart home sync");

        applyWeatherRulesToAllRadiators();
        applySchedulesToAllBlinds();

        log.info("Smart home sync finished");
    }

    private Optional<RadiatorRule> findMatchingRule(BigDecimal outsideTemp, List<RadiatorRule> rules) {
        return rules.stream()
                .filter(rule -> isInsideRange(outsideTemp, rule))
                .min(Comparator.comparing(rule ->
                        rule.getMaxOutsideTemp().subtract(rule.getMinOutsideTemp())
                ));
    }

    private boolean isInsideRange(BigDecimal outsideTemp, RadiatorRule rule) {
        return outsideTemp.compareTo(rule.getMinOutsideTemp()) >= 0
                && outsideTemp.compareTo(rule.getMaxOutsideTemp()) <= 0;
    }

    private boolean isInsideSchedule(LocalTime now, Schedule schedule) {
        return !now.isBefore(schedule.getOpenAt()) && now.isBefore(schedule.getCloseAt());
    }
}