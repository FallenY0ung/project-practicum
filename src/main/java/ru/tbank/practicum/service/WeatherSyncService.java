package ru.tbank.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.dto.OpenWeatherResponse;
import ru.tbank.practicum.entity.Weather;
import ru.tbank.practicum.repositories.WeatherRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherSyncService {

    private final WeatherClient weatherClient;
    private final WeatherRepository weatherRepository;
    private final LogService logService;

    @Transactional
    public Weather fetchAndSaveCurrentWeather(String city) {
        OpenWeatherResponse response = weatherClient.getCurrent(city);

        Weather weather = mapToEntity(response);

        Weather saved = weatherRepository.save(weather);

        log.info("Weather saved for city {}", city);

        return saved;
    }

    private Weather mapToEntity(OpenWeatherResponse response) {
        return Weather.builder()
                .temp(response.main().temp())
                .feelsLike(response.main().feelsLike())
                .name(response.name())
                .description(response.weather().getFirst().description())
                .pressure(Long.valueOf(response.main().pressure()))
                .humidity(Long.valueOf(response.main().humidity()))
                .windSpeed(response.wind().speed())
                .build();
    }
}
