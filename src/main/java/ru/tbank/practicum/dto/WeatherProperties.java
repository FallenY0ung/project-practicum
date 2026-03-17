package ru.tbank.practicum.dto;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather")
public record WeatherProperties(String baseUrl, String apiKey, String city, Duration pollInterval) {}
