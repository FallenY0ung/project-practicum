package ru.tbank.practicum.kafka.dto;

import ru.tbank.practicum.enums.EventSource;

import java.time.Instant;

public record BlindsCommandMessage(
        Long deviceId,
        String command,
        EventSource source,
        Instant timestamp
) {
}