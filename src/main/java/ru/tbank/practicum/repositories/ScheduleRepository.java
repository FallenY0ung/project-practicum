package ru.tbank.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.Schedule;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByBlindsIdAndEnabledTrue(Long blindsId);
}
