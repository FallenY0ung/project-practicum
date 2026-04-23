package ru.tbank.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.kafka.dto.DeviceEventMessage;
import ru.tbank.practicum.service.BlindsService;
import ru.tbank.practicum.service.RadiatorService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaDeviceEventListener {

    private final BlindsService blindsService;
    private final RadiatorService radiatorService;

    @KafkaListener(topics = "devices.events", groupId = "homeapp-device-events")
    public void listen(DeviceEventMessage message) {
        log.info("Received device event from Kafka: {}", message);

        if (message.deviceType() == DeviceType.BLINDS) {
            handleBlindsEvent(message);
        } else if (message.deviceType() == DeviceType.RADIATOR) {
            handleRadiatorEvent(message);
        }
    }

    private void handleBlindsEvent(DeviceEventMessage message) {
        switch (message.eventType()) {
            case "BROKEN" -> blindsService.markAsBroken(message.deviceId(), EventSource.SYSTEM);
            case "OFFLINE" -> blindsService.changeOnlineStatus(message.deviceId(), false, EventSource.SYSTEM);
            case "ONLINE" -> blindsService.changeOnlineStatus(message.deviceId(), true, EventSource.SYSTEM);
            default -> log.warn("Unknown blinds event type: {}", message.eventType());
        }
    }

    private void handleRadiatorEvent(DeviceEventMessage message) {
        switch (message.eventType()) {
            case "BROKEN" -> radiatorService.markAsBroken(message.deviceId(), EventSource.SYSTEM);
            case "OFFLINE" -> radiatorService.changeOnlineStatus(message.deviceId(), false, EventSource.SYSTEM);
            case "ONLINE" -> radiatorService.changeOnlineStatus(message.deviceId(), true, EventSource.SYSTEM);
            default -> log.warn("Unknown radiator event type: {}", message.eventType());
        }
    }
}
