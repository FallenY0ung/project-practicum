package ru.tbank.practicum.kafka.dto;

import java.time.Instant;
import ru.tbank.practicum.enums.DeviceType;

public record DeviceEventMessage(
        DeviceType deviceType, Long deviceId, String eventType, String message, Instant timestamp) {}
