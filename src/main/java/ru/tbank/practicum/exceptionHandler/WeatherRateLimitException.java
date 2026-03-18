package ru.tbank.practicum.exceptionHandler;

public class WeatherRateLimitException extends RuntimeException {
    public WeatherRateLimitException(String message) {
        super(message);
    }
}
