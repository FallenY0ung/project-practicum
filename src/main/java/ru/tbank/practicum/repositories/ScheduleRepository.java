package ru.tbank.practicum.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByBlindsIdAndEnabledTrue(Long blindsId);

    List<Schedule> findByBlindsId(Long blindsId);
}
