package ru.tbank.practicum.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tbank.practicum.dto.OpenWeatherResponse;
import ru.tbank.practicum.dto.WeatherProperties;
import ru.tbank.practicum.service.SmartHomeService;
import ru.tbank.practicum.service.WeatherClient;
import ru.tbank.practicum.service.WeatherSyncService;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherScheduler {

    private final WeatherProperties props;
    private final WeatherSyncService weatherSyncService;
    private final SmartHomeService smartHomeService;

    @Scheduled(fixedDelayString = "${weather.poll-interval:1m}")
    public void pollWeather() {
        log.debug("Starting weather poll for city {}", props.city());
        try {
            weatherSyncService.fetchAndSaveCurrentWeather(props.city());
            log.info("Weather sync completed for city {}", props.city());

            smartHomeService.applyWeatherRulesToAllRadiators();
            log.info("Weather rules applied to all radiators");
        } catch (Exception e) {
            log.warn("Weather poll failed", e);
        }
    }
}
