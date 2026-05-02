package ru.tbank.practicum.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openrouter")
public record OpenRouterProperties(String apiKey, String baseUrl, String model, String siteUrl, String siteName) {}
