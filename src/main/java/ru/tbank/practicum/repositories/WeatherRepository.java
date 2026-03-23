package ru.tbank.practicum.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.Weather;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findTopByOrderByRecordedAtDesc();
}
