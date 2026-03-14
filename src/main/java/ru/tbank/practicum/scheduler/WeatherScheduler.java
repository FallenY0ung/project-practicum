package ru.tbank.practicum.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tbank.practicum.dto.OpenWeatherResponse;
import ru.tbank.practicum.dto.WeatherProperties;
import ru.tbank.practicum.service.WeatherClient;

@Component
public class WeatherScheduler {
    private static final Logger log = LoggerFactory.getLogger(WeatherScheduler.class);

    private final WeatherClient weatherClient;
    private final WeatherProperties props;

    public WeatherScheduler(WeatherClient weatherClient, WeatherProperties props) {
        this.weatherClient = weatherClient;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${weather.poll-interval:PT10M}")
    public void pollWeather() {
        log.debug("Starting weather poll for city {}", props.city());
        try {
            OpenWeatherResponse resp = weatherClient.getCurrent(props.city());
            double temp = resp.main().temp();
            String desc =
                    resp.weather().isEmpty() ? "n/a" : resp.weather().get(0).description();
            log.info("Weather in {}: {}°C, {}", props.city(), temp, desc);
        } catch (Exception e) {
            log.warn("Weather poll failed: {}", e.toString());
        }
    }
}
