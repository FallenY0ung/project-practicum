package ru.tbank.practicum.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.practicum.entity.DeviceEvent;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.EventType;
import ru.tbank.practicum.repositories.DeviceEventRepository;

@ExtendWith(MockitoExtension.class)
class DeviceEventServiceTest {

    @Mock
    private DeviceEventRepository deviceEventRepository;

    @InjectMocks
    private DeviceEventService deviceEventService;

    @DisplayName("Должен вернуть DeviceEvent по id")
    @Test
    void shouldReturnDeviceEventById() {
        DeviceEvent deviceEvent = createDeviceEvent(
                1L, DeviceType.BLINDS, 10L, EventType.BLINDS_OPENED, EventSource.USER, "Blinds opened");

        when(deviceEventRepository.findById(1L)).thenReturn(Optional.of(deviceEvent));

        DeviceEvent result = deviceEventService.getById(1L);

        assertSame(deviceEvent, result);
        assertEquals(1L, result.getId());
        assertEquals(DeviceType.BLINDS, result.getDeviceType());

        verify(deviceEventRepository).findById(1L);
    }

    @DisplayName("Должен вернуть все DeviceEvent")
    @Test
    void shouldReturnAllDeviceEvents() {
        DeviceEvent event1 = createDeviceEvent(
                1L, DeviceType.BLINDS, 10L, EventType.BLINDS_OPENED, EventSource.USER, "Blinds opened");

        DeviceEvent event2 = createDeviceEvent(
                2L, DeviceType.BLINDS, 11L, EventType.BLINDS_CLOSED, EventSource.SYSTEM, "Blinds closed");

        List<DeviceEvent> events = List.of(event1, event2);

        when(deviceEventRepository.findAll()).thenReturn(events);

        List<DeviceEvent> result = deviceEventService.getAll();

        assertEquals(2, result.size());
        assertSame(events, result);

        verify(deviceEventRepository).findAll();
    }

    @DisplayName("Должен вернуть события по устройству")
    @Test
    void shouldReturnEventsByDevice() {
        DeviceType deviceType = DeviceType.BLINDS;
        Long deviceId = 10L;

        DeviceEvent event1 =
                createDeviceEvent(1L, deviceType, deviceId, EventType.BLINDS_OPENED, EventSource.USER, "Blinds opened");

        DeviceEvent event2 = createDeviceEvent(
                2L, deviceType, deviceId, EventType.BLINDS_CLOSED, EventSource.SYSTEM, "Blinds closed");

        List<DeviceEvent> events = List.of(event1, event2);

        when(deviceEventRepository.findByDeviceTypeAndDeviceIdOrderByCreatedAtDesc(deviceType, deviceId))
                .thenReturn(events);

        List<DeviceEvent> result = deviceEventService.getByDevice(deviceType, deviceId);

        assertEquals(2, result.size());
        assertSame(events, result);

        verify(deviceEventRepository).findByDeviceTypeAndDeviceIdOrderByCreatedAtDesc(deviceType, deviceId);
    }

    @DisplayName("Должен сохранить DeviceEvent")
    @Test
    void shouldSaveDeviceEvent() {
        DeviceEvent deviceEvent = createDeviceEvent(
                null, DeviceType.BLINDS, 10L, EventType.BLINDS_OPENED, EventSource.USER, "Blinds opened");

        DeviceEvent savedEvent = createDeviceEvent(
                1L, DeviceType.BLINDS, 10L, EventType.BLINDS_OPENED, EventSource.USER, "Blinds opened");

        when(deviceEventRepository.save(deviceEvent)).thenReturn(savedEvent);

        DeviceEvent result = deviceEventService.save(deviceEvent);

        assertSame(savedEvent, result);
        assertEquals(1L, result.getId());
        assertEquals("Blinds opened", result.getMessage());

        verify(deviceEventRepository).save(deviceEvent);
    }

    @DisplayName("Должен создать и сохранить DeviceEvent")
    @Test
    void shouldCreateAndSaveDeviceEvent() {
        DeviceType deviceType = DeviceType.BLINDS;
        Long deviceId = 10L;
        EventType eventType = EventType.BLINDS_OPENED;
        EventSource source = EventSource.USER;
        String message = "Blinds opened";

        DeviceEvent savedEvent = createDeviceEvent(1L, deviceType, deviceId, eventType, source, message);

        when(deviceEventRepository.save(any(DeviceEvent.class))).thenReturn(savedEvent);

        DeviceEvent result = deviceEventService.createEvent(deviceType, deviceId, eventType, source, message);

        assertSame(savedEvent, result);
        assertEquals(1L, result.getId());

        ArgumentCaptor<DeviceEvent> captor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(deviceEventRepository).save(captor.capture());

        DeviceEvent captured = captor.getValue();
        assertEquals(deviceType, captured.getDeviceType());
        assertEquals(deviceId, captured.getDeviceId());
        assertEquals(eventType, captured.getEventType());
        assertEquals(source, captured.getSource());
        assertEquals(message, captured.getMessage());
    }

    private DeviceEvent createDeviceEvent(
            Long id, DeviceType deviceType, Long deviceId, EventType eventType, EventSource source, String message) {
        DeviceEvent deviceEvent = DeviceEvent.builder()
                .deviceType(deviceType)
                .deviceId(deviceId)
                .eventType(eventType)
                .source(source)
                .message(message)
                .build();

        deviceEvent.setId(id);
        return deviceEvent;
    }
}
