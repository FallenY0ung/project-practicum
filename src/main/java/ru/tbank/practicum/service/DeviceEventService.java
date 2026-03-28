package ru.tbank.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.entity.DeviceEvent;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.EventType;
import ru.tbank.practicum.repositories.DeviceEventRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceEventService {

    private final DeviceEventRepository deviceEventRepository;

    @Transactional(readOnly = true)
    public DeviceEvent getById(Long id) {
        return deviceEventRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DeviceEvent with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<DeviceEvent> getAll() {
        return deviceEventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DeviceEvent> getByDevice(DeviceType deviceType, Long deviceId) {
        return deviceEventRepository.findByDeviceTypeAndDeviceIdOrderByCreatedAtDesc(deviceType, deviceId);
    }

    public DeviceEvent save(DeviceEvent deviceEvent) {
        validateDeviceEvent(deviceEvent);
        return deviceEventRepository.save(deviceEvent);
    }

    public DeviceEvent createEvent(
            DeviceType deviceType, Long deviceId, EventType eventType, EventSource source, String message) {
        validate(deviceType, deviceId, eventType, source, message);

        DeviceEvent deviceEvent = DeviceEvent.builder()
                .deviceType(deviceType)
                .deviceId(deviceId)
                .eventType(eventType)
                .source(source)
                .message(message)
                .build();

        return deviceEventRepository.save(deviceEvent);
    }

    private void validateDeviceEvent(DeviceEvent deviceEvent) {
        if (deviceEvent == null) {
            throw new IllegalArgumentException("DeviceEvent cannot be null");
        }

        validate(
                deviceEvent.getDeviceType(),
                deviceEvent.getDeviceId(),
                deviceEvent.getEventType(),
                deviceEvent.getSource(),
                deviceEvent.getMessage());
    }

    private void validate(
            DeviceType deviceType, Long deviceId, EventType eventType, EventSource source, String message) {
        if (deviceType == null) {
            throw new IllegalArgumentException("Device type cannot be null");
        }
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("Device id must be positive");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (source == null) {
            throw new IllegalArgumentException("Event source cannot be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be blank");
        }
    }
}
