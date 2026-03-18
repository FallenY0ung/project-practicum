package ru.tbank.practicum.dto;

import java.time.LocalTime;

public record CurtainsSchedule(LocalTime openAt, LocalTime closeAt, boolean enabled) {}
