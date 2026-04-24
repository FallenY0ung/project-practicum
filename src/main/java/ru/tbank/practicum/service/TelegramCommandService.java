package ru.tbank.practicum.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.practicum.dto.telegram.TelegramMessageFormatter;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.Radiator;
import ru.tbank.practicum.entity.Weather;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramCommandService {

    private final TelegramMessageFormatter formatter;
    private final WeatherService weatherService;
    private final RadiatorService radiatorService;
    private final BlindsService blindsService;
    private final WeatherAdviceService weatherAdviceService;

    public String handleCommand(String text) {
        if (text == null || text.isBlank()) {
            return "Пустая команда.";
        }

        return switch (text.trim()) {
            case "/start", "/help" -> """
                    Доступные команды:
                    /weather — текущая погода
                    /status — статус дома
                    /advice — совет от ИИ по погоде
                    """;
            case "/weather" -> {
                Weather weather = getLatestWeatherSafe();
                yield formatter.buildWeatherMessage(weather);
            }
            case "/status" -> {
                Weather weather = getLatestWeatherSafe();
                List<Radiator> radiators = radiatorService.getAll();
                List<Blinds> blinds = blindsService.getAll();
                yield formatter.buildStatusMessage(weather, radiators, blinds);
            }
            case "/advice" -> {
                Weather weather = getLatestWeatherSafe();
                yield buildAdviceMessage(weather);
            }
            default -> "Неизвестная команда. Используй /help";
        };
    }

    private Weather getLatestWeatherSafe() {
        try {
            return weatherService.getLatest();
        } catch (Exception e) {
            log.warn("Failed to load weather for Telegram bot", e);
            return null;
        }
    }

    private String buildAdviceMessage(Weather weather) {
        if (weather == null) {
            return "Нет данных о погоде, чтобы получить совет от ИИ.";
        }

        try {
            String advice = weatherAdviceService.getAdvice(weather);
            return "Совет от ИИ по погоде:\n\n" + advice;
        } catch (Exception e) {
            log.warn("Failed to get AI advice for Telegram bot", e);
            return "Не удалось получить совет от ИИ.";
        }
    }
}