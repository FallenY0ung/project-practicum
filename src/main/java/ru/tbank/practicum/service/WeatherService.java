package ru.tbank.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.entity.Weather;
import ru.tbank.practicum.repositories.WeatherRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;

    @Transactional(readOnly = true)
    public Weather getById(Long id) {
        return weatherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Weather> getAll() {
        return weatherRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Weather getLatest() {
        return weatherRepository.findTopByOrderByRecordedAtDesc()
                .orElseThrow(() -> new EntityNotFoundException("No weather records found"));
    }

    public Weather save(Weather weather) {
        validateWeather(weather);
        return weatherRepository.save(weather);
    }

    public Weather updateWeather(
            Long id,
            BigDecimal temp,
            BigDecimal feelsLike,
            String name,
            String description,
            Long pressure,
            Long humidity,
            BigDecimal windSpeed
    ) {
        Weather weather = getById(id);

        weather.setTemp(temp);
        weather.setFeelsLike(feelsLike);
        weather.setName(name);
        weather.setDescription(description);
        weather.setPressure(pressure);
        weather.setHumidity(humidity);
        weather.setWindSpeed(windSpeed);

        validateWeather(weather);
        return weather;
    }

    private void validateWeather(Weather weather) {
        if (weather.getTemp() == null ||
                weather.getFeelsLike() == null ||
                weather.getName() == null ||
                weather.getDescription() == null ||
                weather.getPressure() == null ||
                weather.getHumidity() == null ||
                weather.getWindSpeed() == null) {
            throw new IllegalArgumentException("Weather fields cannot be null");
        }

        if (weather.getHumidity() < 0 || weather.getHumidity() > 100) {
            throw new IllegalArgumentException("Humidity must be between 0 and 100");
        }

        if (weather.getPressure() < 0) {
            throw new IllegalArgumentException("Pressure cannot be negative");
        }

        if (weather.getWindSpeed().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Wind speed cannot be negative");
        }

        if (weather.getName().isBlank()) {
            throw new IllegalArgumentException("Weather name cannot be blank");
        }

        if (weather.getDescription().isBlank()) {
            throw new IllegalArgumentException("Weather description cannot be blank");
        }
    }
}
