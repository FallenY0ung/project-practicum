package ru.tbank.practicum.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.DeviceEvent;
import ru.tbank.practicum.enums.DeviceType;

public interface DeviceEventRepository extends JpaRepository<DeviceEvent, Long> {
    List<DeviceEvent> findByDeviceTypeAndDeviceIdOrderByCreatedAtDesc(DeviceType deviceType, Long deviceId);
}
