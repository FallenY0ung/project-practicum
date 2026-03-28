package ru.tbank.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.practicum.entity.Weather;
import ru.tbank.practicum.repositories.WeatherRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;

    @Transactional(readOnly = true)
    public Weather getById(Long id) {
        return weatherRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Weather> getAll() {
        return weatherRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Weather getLatest() {
        return weatherRepository
                .findTopByOrderByRecordedAtDesc()
                .orElseThrow(() -> new EntityNotFoundException("No weather records found"));
    }

    public Weather save(Weather weather) {
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
            BigDecimal windSpeed) {
        Weather weather = getById(id);

        weather.setTemp(temp);
        weather.setFeelsLike(feelsLike);
        weather.setName(name);
        weather.setDescription(description);
        weather.setPressure(pressure);
        weather.setHumidity(humidity);
        weather.setWindSpeed(windSpeed);

        return weather;
    }
}
