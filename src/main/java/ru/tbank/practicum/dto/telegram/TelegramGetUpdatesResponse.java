package ru.tbank.practicum.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramGetUpdatesResponse(boolean ok, List<TelegramUpdate> result) {}
