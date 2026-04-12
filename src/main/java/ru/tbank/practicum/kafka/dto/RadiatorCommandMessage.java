package ru.tbank.practicum.kafka.dto;

import ru.tbank.practicum.enums.EventSource;

import java.math.BigDecimal;
import java.time.Instant;

public record RadiatorCommandMessage(
        Long deviceId,
        BigDecimal targetTemperature,
        EventSource source,
        Instant timestamp
) {
}