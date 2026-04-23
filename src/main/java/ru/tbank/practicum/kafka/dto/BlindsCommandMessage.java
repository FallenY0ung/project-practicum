package ru.tbank.practicum.kafka.dto;

import java.time.Instant;
import ru.tbank.practicum.enums.EventSource;

public record BlindsCommandMessage(Long deviceId, String command, EventSource source, Instant timestamp) {}
