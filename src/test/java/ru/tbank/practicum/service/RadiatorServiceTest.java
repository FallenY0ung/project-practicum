package ru.tbank.practicum.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.practicum.entity.Radiator;
import ru.tbank.practicum.entity.RadiatorRule;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.EventType;
import ru.tbank.practicum.enums.LogStatus;
import ru.tbank.practicum.kafka.KafkaCommandProducer;
import ru.tbank.practicum.repositories.RadiatorRepository;
import ru.tbank.practicum.repositories.RadiatorRuleRepository;

@ExtendWith(MockitoExtension.class)
class RadiatorServiceTest {

    @Mock
    private RadiatorRepository radiatorRepository;

    @Mock
    private DeviceEventService deviceEventService;

    @Mock
    private LogService logService;

    @Mock
    private RadiatorRuleRepository radiatorRuleRepository;

    @Mock
    private KafkaCommandProducer kafkaCommandProducer;

    @InjectMocks
    private RadiatorService radiatorService;

    @DisplayName("Должен вернуть radiator по id")
    @Test
    void shouldReturnRadiatorById() {
        Radiator radiator = createRadiator(1L, "22.5", true, false);

        when(radiatorRepository.findById(1L)).thenReturn(Optional.of(radiator));

        Radiator result = radiatorService.getById(1L);

        assertSame(radiator, result);
        assertEquals(1L, result.getId());

        verify(radiatorRepository).findById(1L);
    }

    @DisplayName("Должен вернуть все radiator")
    @Test
    void shouldReturnAllRadiators() {
        Radiator radiator1 = createRadiator(1L, "22.5", true, false);
        Radiator radiator2 = createRadiator(2L, "24.0", false, false);

        List<Radiator> radiators = List.of(radiator1, radiator2);

        when(radiatorRepository.findAll()).thenReturn(radiators);

        List<Radiator> result = radiatorService.getAll();

        assertSame(radiators, result);
        assertEquals(2, result.size());

        verify(radiatorRepository).findAll();
    }

    @DisplayName("Должен вернуть правила по radiatorId")
    @Test
    void shouldReturnRulesByRadiatorId() {
        Long radiatorId = 10L;

        RadiatorRule rule1 = createRadiatorRule(1L, radiatorId);
        RadiatorRule rule2 = createRadiatorRule(2L, radiatorId);

        List<RadiatorRule> rules = List.of(rule1, rule2);

        when(radiatorRuleRepository.findByRadiatorId(radiatorId)).thenReturn(rules);

        List<RadiatorRule> result = radiatorService.getByRadiatorId(radiatorId);

        assertSame(rules, result);
        assertEquals(2, result.size());

        verify(radiatorRuleRepository).findByRadiatorId(radiatorId);
    }

    @DisplayName("Должен сохранить radiator и создать лог")
    @Test
    void shouldSaveRadiator() {
        Radiator radiator = createRadiator(null, "22.5", true, false);
        Radiator saved = createRadiator(1L, "22.5", true, false);

        when(radiatorRepository.save(radiator)).thenReturn(saved);

        Radiator result = radiatorService.save(radiator, EventSource.USER);

        assertSame(saved, result);
        assertEquals(1L, result.getId());

        verify(radiatorRepository).save(radiator);
        verify(logService)
                .createLog(
                        DeviceType.RADIATOR,
                        1L,
                        LogStatus.SUCCESS,
                        EventSource.USER,
                        "CREATE_RADIATOR",
                        "Radiator was created successfully");
    }

    @DisplayName("Должен удалить radiator и создать лог")
    @Test
    void shouldDeleteRadiatorById() {
        Long id = 1L;
        Radiator radiator = createRadiator(id, "22.5", true, false);

        when(radiatorRepository.findById(id)).thenReturn(Optional.of(radiator));
        when(radiatorRuleRepository.findByRadiatorIdAndEnabledTrue(id)).thenReturn(List.of());

        radiatorService.deleteById(id);

        verify(radiatorRepository).findById(id);
        verify(radiatorRuleRepository).findByRadiatorIdAndEnabledTrue(id);
        verify(logService)
                .createLog(
                        DeviceType.RADIATOR,
                        id,
                        LogStatus.WARNING,
                        EventSource.USER,
                        "DELETE_RADIATOR",
                        "Radiator was deleted");
        verify(radiatorRepository).delete(radiator);
    }

    @DisplayName("Должен обновить температуру, создать event и log")
    @Test
    void shouldUpdateTemperature() {
        Long id = 1L;
        Radiator radiator = createRadiator(id, "22.5", true, false);
        BigDecimal newTemp = new BigDecimal("25.0");

        when(radiatorRepository.findById(id)).thenReturn(Optional.of(radiator));

        Radiator result = radiatorService.updateTemperature(id, newTemp, EventSource.USER);

        assertSame(radiator, result);
        assertEquals(newTemp, result.getTemp());

        verify(radiatorRepository).findById(id);
        verify(deviceEventService)
                .createEvent(
                        DeviceType.RADIATOR,
                        id,
                        EventType.RADIATOR_TEMPERATURE_SET,
                        EventSource.USER,
                        "Radiator temperature changed from 22.5 to 25.0");
        verify(logService)
                .createLog(
                        DeviceType.RADIATOR,
                        id,
                        LogStatus.SUCCESS,
                        EventSource.USER,
                        "UPDATE_TEMPERATURE",
                        "Radiator temperature changed from 22.5 to 25.0");
    }

    @DisplayName("Должен пометить radiator как сломанный, создать event и log")
    @Test
    void shouldMarkRadiatorAsBroken() {
        Long id = 1L;
        Radiator radiator = createRadiator(id, "22.5", true, false);

        when(radiatorRepository.findById(id)).thenReturn(Optional.of(radiator));

        Radiator result = radiatorService.markAsBroken(id, EventSource.USER);

        assertSame(radiator, result);
        assertTrue(result.getIsBroken());

        verify(radiatorRepository).findById(id);
        verify(deviceEventService)
                .createEvent(
                        DeviceType.RADIATOR,
                        id,
                        EventType.RADIATOR_BROKEN,
                        EventSource.USER,
                        "Radiator marked as broken");
        verify(logService)
                .createLog(
                        DeviceType.RADIATOR,
                        id,
                        LogStatus.WARNING,
                        EventSource.USER,
                        "MARK_AS_BROKEN",
                        "Radiator status changed to broken");
    }

    @DisplayName("Должен восстановить radiator, создать event и log")
    @Test
    void shouldRestoreRadiator() {
        Long id = 1L;
        Radiator radiator = createRadiator(id, "22.5", true, true);

        when(radiatorRepository.findById(id)).thenReturn(Optional.of(radiator));

        Radiator result = radiatorService.restore(id, EventSource.USER);

        assertSame(radiator, result);
        assertFalse(result.getIsBroken());

        verify(radiatorRepository).findById(id);
        verify(deviceEventService)
                .createEvent(
                        DeviceType.RADIATOR, id, EventType.RADIATOR_RESTORED, EventSource.USER, "Radiator restored");
        verify(logService)
                .createLog(
                        DeviceType.RADIATOR,
                        id,
                        LogStatus.SUCCESS,
                        EventSource.USER,
                        "RESTORE_RADIATOR",
                        "Radiator restored successfully");
    }

    @DisplayName("Должен изменить online status, создать event и log")
    @Test
    void shouldChangeOnlineStatus() {
        Long id = 1L;
        Radiator radiator = createRadiator(id, "22.5", true, false);

        when(radiatorRepository.findById(id)).thenReturn(Optional.of(radiator));

        Radiator result = radiatorService.changeOnlineStatus(id, false, EventSource.USER);

        assertSame(radiator, result);
        assertFalse(result.getIsOnline());

        verify(radiatorRepository).findById(id);
        verify(deviceEventService)
                .createEvent(
                        DeviceType.RADIATOR,
                        id,
                        EventType.RADIATOR_OFFLINE,
                        EventSource.USER,
                        "Radiator online status changed to false");
        verify(logService)
                .createLog(
                        DeviceType.RADIATOR,
                        id,
                        LogStatus.SUCCESS,
                        EventSource.USER,
                        "CHANGE_ONLINE_STATUS",
                        "Radiator online status updated successfully");
    }

    @DisplayName("Должен вызвать нужные методы в updateFromForm")
    @Test
    void shouldUpdateFromForm() {
        RadiatorService spyService = spy(new RadiatorService(
                radiatorRepository, deviceEventService, logService, radiatorRuleRepository, kafkaCommandProducer));

        Long id = 1L;
        EventSource source = EventSource.USER;

        Radiator current = createRadiator(id, "22.5", true, false);
        Radiator updated = createRadiator(id, "25.0", false, true);

        doReturn(current).when(spyService).getById(id);
        doReturn(current).when(spyService).updateTemperature(id, new BigDecimal("25.0"), source);
        doReturn(current).when(spyService).markAsBroken(id, source);
        doReturn(current).when(spyService).changeOnlineStatus(id, false, source);

        spyService.updateFromForm(id, updated, source);

        verify(spyService).updateTemperature(id, new BigDecimal("25.0"), source);
        verify(spyService).markAsBroken(id, source);
        verify(spyService).changeOnlineStatus(id, false, source);
        verify(spyService, never()).restore(anyLong(), any());
    }

    private Radiator createRadiator(Long id, String temp, Boolean isOnline, Boolean isBroken) {
        Radiator radiator = new Radiator();
        radiator.setId(id);
        radiator.setTemp(new BigDecimal(temp));
        radiator.setIsOnline(isOnline);
        radiator.setIsBroken(isBroken);
        return radiator;
    }

    private RadiatorRule createRadiatorRule(Long ruleId, Long radiatorId) {
        Radiator radiator = new Radiator();
        radiator.setId(radiatorId);

        RadiatorRule rule = new RadiatorRule();
        rule.setId(ruleId);
        rule.setRadiator(radiator);
        return rule;
    }
}
