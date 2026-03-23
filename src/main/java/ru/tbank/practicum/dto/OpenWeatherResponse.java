package ru.tbank.practicum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record OpenWeatherResponse(
        Main main,
        List<WeatherInfo> weather,
        Wind wind,
        String name
) {

    public record Main(
            BigDecimal temp,
            @JsonProperty("feels_like")
            BigDecimal feelsLike,
            Integer pressure,
            Integer humidity
    ) {}

    public record Wind(
            BigDecimal speed
    ) {}

    public record WeatherInfo(
            String description
    ) {}
}