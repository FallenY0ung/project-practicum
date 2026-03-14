package ru.tbank.practicum.dto;

import java.util.List;

public record OpenWeatherResponse(Main main, List<Weather> weather) {
    public record Main(double temp) {}

    public record Weather(String description) {}
}
