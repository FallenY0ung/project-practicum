package ru.tbank.practicum.kafka.dto;

import ru.tbank.practicum.enums.DeviceType;

import java.time.Instant;

public record DeviceEventMessage(
        DeviceType deviceType,
        Long deviceId,
        String eventType,
        String message,
        Instant timestamp
) {
}