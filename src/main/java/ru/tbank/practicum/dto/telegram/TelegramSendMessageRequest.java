package ru.tbank.practicum.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramSendMessageRequest(
        @JsonProperty("chat_id") Long chatId, String text) {}
