package ru.tbank.practicum.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.Radiator;
import ru.tbank.practicum.entity.RadiatorRule;
import ru.tbank.practicum.entity.Schedule;
import ru.tbank.practicum.entity.Weather;
import ru.tbank.practicum.enums.BlindsState;
import ru.tbank.practicum.enums.EventSource;

@ExtendWith(MockitoExtension.class)
class SmartHomeServiceTest {

    @Mock
    private WeatherService weatherService;

    @Mock
    private RadiatorService radiatorService;

    @Mock
    private RadiatorRuleService radiatorRuleService;

    @Mock
    private BlindsService blindsService;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private SmartHomeService smartHomeService;

    @DisplayName("Должен вернуть последнюю погоду")
    @Test
    void shouldReturnLatestWeather() {
        Weather weather = createWeather("5.0");

        when(weatherService.getLatest()).thenReturn(weather);

        Weather result = smartHomeService.getLatestWeather();

        assertSame(weather, result);
        verify(weatherService).getLatest();
    }

    @DisplayName("Должен применить подходящее правило к radiator")
    @Test
    void shouldApplyWeatherRuleToRadiator() {
        Long radiatorId = 1L;

        Radiator radiator = createRadiator(radiatorId, false, true);
        Weather weather = createWeather("5.0");

        RadiatorRule narrowRule = createRadiatorRule(10L, "0", "10", "22");

        RadiatorRule wideRule = createRadiatorRule(11L, "0", "20", "24");

        when(radiatorService.getById(radiatorId)).thenReturn(radiator);
        when(weatherService.getLatest()).thenReturn(weather);
        when(radiatorRuleService.getActiveRulesByRadiatorId(radiatorId)).thenReturn(List.of(wideRule, narrowRule));

        smartHomeService.applyWeatherRulesToRadiator(radiatorId);

        verify(radiatorService).getById(radiatorId);
        verify(weatherService).getLatest();
        verify(radiatorRuleService).getActiveRulesByRadiatorId(radiatorId);

        verify(radiatorService).updateTemperature(radiatorId, new BigDecimal("22"), EventSource.WEATHER_RULE);
    }

    @DisplayName("Должен применить weather rules ко всем radiator")
    @Test
    void shouldApplyWeatherRulesToAllRadiators() {
        SmartHomeService spyService = spy(new SmartHomeService(
                weatherService, radiatorService, radiatorRuleService, blindsService, scheduleService));

        Radiator radiator1 = createRadiator(1L, false, true);
        Radiator radiator2 = createRadiator(2L, false, true);

        when(radiatorService.getAll()).thenReturn(List.of(radiator1, radiator2));
        doNothing().when(spyService).applyWeatherRulesToRadiator(anyLong());

        spyService.applyWeatherRulesToAllRadiators();

        verify(radiatorService).getAll();
        verify(spyService).applyWeatherRulesToRadiator(1L);
        verify(spyService).applyWeatherRulesToRadiator(2L);
    }

    @DisplayName("Должен открыть blinds по активному schedule")
    @Test
    void shouldApplyScheduleToBlinds() {
        Long blindsId = 1L;

        Blinds blinds = createBlinds(blindsId, false, true);

        Schedule activeSchedule = createSchedule(blindsId, LocalTime.MIN, LocalTime.MAX);

        when(blindsService.getById(blindsId)).thenReturn(blinds);
        when(scheduleService.getActiveByBlindsId(blindsId)).thenReturn(List.of(activeSchedule));

        smartHomeService.applyScheduleToBlinds(blindsId);

        verify(blindsService).getById(blindsId);
        verify(scheduleService).getActiveByBlindsId(blindsId);
        verify(blindsService).updateState(blindsId, BlindsState.OPEN, EventSource.SCHEDULE);
    }

    @DisplayName("Должен применить schedules ко всем blinds")
    @Test
    void shouldApplySchedulesToAllBlinds() {
        SmartHomeService spyService = spy(new SmartHomeService(
                weatherService, radiatorService, radiatorRuleService, blindsService, scheduleService));

        Blinds blinds1 = createBlinds(1L, false, true);
        Blinds blinds2 = createBlinds(2L, false, true);

        when(blindsService.getAll()).thenReturn(List.of(blinds1, blinds2));
        doNothing().when(spyService).applyScheduleToBlinds(anyLong());

        spyService.applySchedulesToAllBlinds();

        verify(blindsService).getAll();
        verify(spyService).applyScheduleToBlinds(1L);
        verify(spyService).applyScheduleToBlinds(2L);
    }

    @DisplayName("Должен выполнить полный sync всех устройств")
    @Test
    void shouldSyncAllDevices() {
        SmartHomeService spyService = spy(new SmartHomeService(
                weatherService, radiatorService, radiatorRuleService, blindsService, scheduleService));

        doNothing().when(spyService).applyWeatherRulesToAllRadiators();
        doNothing().when(spyService).applySchedulesToAllBlinds();

        spyService.syncAllDevices();

        verify(spyService).applyWeatherRulesToAllRadiators();
        verify(spyService).applySchedulesToAllBlinds();
    }

    private Weather createWeather(String temp) {
        Weather weather = new Weather();
        weather.setTemp(new BigDecimal(temp));
        return weather;
    }

    private Radiator createRadiator(Long id, Boolean isBroken, Boolean isOnline) {
        Radiator radiator = new Radiator();
        radiator.setId(id);
        radiator.setIsBroken(isBroken);
        radiator.setIsOnline(isOnline);
        return radiator;
    }

    private RadiatorRule createRadiatorRule(Long id, String minOutsideTemp, String maxOutsideTemp, String targetTemp) {
        RadiatorRule rule = new RadiatorRule();
        rule.setId(id);
        rule.setMinOutsideTemp(new BigDecimal(minOutsideTemp));
        rule.setMaxOutsideTemp(new BigDecimal(maxOutsideTemp));
        rule.setTargetRadiatorTemp(new BigDecimal(targetTemp));
        return rule;
    }

    private Blinds createBlinds(Long id, Boolean isBroken, Boolean isOnline) {
        Blinds blinds = new Blinds();
        blinds.setId(id);
        blinds.setIsBroken(isBroken);
        blinds.setIsOnline(isOnline);
        return blinds;
    }

    private Schedule createSchedule(Long blindsId, LocalTime openAt, LocalTime closeAt) {
        Blinds blinds = new Blinds();
        blinds.setId(blindsId);

        Schedule schedule = new Schedule();
        schedule.setBlinds(blinds);
        schedule.setOpenAt(openAt);
        schedule.setCloseAt(closeAt);
        schedule.setEnabled(true);
        return schedule;
    }
}
