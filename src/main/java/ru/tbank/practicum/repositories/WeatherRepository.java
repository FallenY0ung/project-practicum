package ru.tbank.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.Weather;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findTopByOrderByRecordedAtDesc();
}
