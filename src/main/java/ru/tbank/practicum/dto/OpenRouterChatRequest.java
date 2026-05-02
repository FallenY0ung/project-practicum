package ru.tbank.practicum.dto;

import java.util.List;

public record OpenRouterChatRequest(String model, List<Message> messages) {
    public record Message(String role, String content) {}
}
