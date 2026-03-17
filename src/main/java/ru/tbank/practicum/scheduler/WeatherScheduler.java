package ru.tbank.practicum.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tbank.practicum.dto.OpenWeatherResponse;
import ru.tbank.practicum.dto.WeatherProperties;
import ru.tbank.practicum.service.WeatherClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherScheduler {

    private final WeatherClient weatherClient;
    private final WeatherProperties props;

    @Scheduled(fixedDelayString = "${weather.poll-interval:1m}")
    public void pollWeather() {
        log.debug("Starting weather poll for city {}", props.city());
        try {
            OpenWeatherResponse resp = weatherClient.getCurrent(props.city());
            double temp = resp.main().temp();
            String desc =
                    resp.weather().isEmpty() ? "n/a" : resp.weather().get(0).description();
            log.info("Weather in {}: {}°C, {}", props.city(), temp, desc);
        } catch (Exception e) {
            log.warn("Weather poll failed", e);
        }
    }
}
