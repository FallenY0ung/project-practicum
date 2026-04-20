package ru.tbank.practicum.dto;

import java.util.List;

public record OpenRouterChatResponse(
        List<Choice> choices
) {
    public record Choice(
            Message message
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }
}