package ru.tbank.practicum.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;
import ru.tbank.practicum.enums.EventSource;

public record RadiatorCommandMessage(
        Long deviceId, BigDecimal targetTemperature, EventSource source, Instant timestamp) {}
