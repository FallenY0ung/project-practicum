package ru.tbank.practicum.dto.telegram;

public record TelegramSendMessageRequest(Long chat_id, String text) {}
