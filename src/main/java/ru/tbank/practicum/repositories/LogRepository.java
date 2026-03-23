package ru.tbank.practicum.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.Log;
import ru.tbank.practicum.enums.DeviceType;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByDeviceTypeAndDeviceIdOrderByCreatedAtDesc(DeviceType deviceType, Long deviceId);
}
