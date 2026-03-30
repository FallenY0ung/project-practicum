package ru.tbank.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.entity.Log;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.LogStatus;
import ru.tbank.practicum.repositories.LogRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    @Transactional(readOnly = true)
    public Log getById(Long id) {
        return logRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Log with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Log> getAll() {
        return logRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Log> getByDevice(DeviceType deviceType, Long deviceId) {
        return logRepository.findByDeviceTypeAndDeviceIdOrderByCreatedAtDesc(deviceType, deviceId);
    }

    public Log save(Log log) {
        validateLog(log);
        return logRepository.save(log);
    }

    public Log createLog(
            DeviceType deviceType, Long deviceId, LogStatus status, EventSource source, String action, String message) {
        validate(deviceType, deviceId, status, source, action, message);

        Log log = Log.builder()
                .deviceType(deviceType)
                .deviceId(deviceId)
                .status(status)
                .source(source)
                .action(action)
                .message(message)
                .build();

        return logRepository.save(log);
    }

    private void validateLog(Log log) {
        if (log == null) {
            throw new IllegalArgumentException("Log cannot be null");
        }

        validate(
                log.getDeviceType(),
                log.getDeviceId(),
                log.getStatus(),
                log.getSource(),
                log.getAction(),
                log.getMessage());
    }

    private void validate(
            DeviceType deviceType, Long deviceId, LogStatus status, EventSource source, String action, String message) {
        if (deviceType == null) {
            throw new IllegalArgumentException("Device type cannot be null");
        }
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("Device id must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Log status cannot be null");
        }
        if (source == null) {
            throw new IllegalArgumentException("Log source cannot be null");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be blank");
        }
    }
}
