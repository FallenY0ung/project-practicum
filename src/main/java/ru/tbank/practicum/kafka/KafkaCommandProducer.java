package ru.tbank.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.tbank.practicum.kafka.dto.BlindsCommandMessage;
import ru.tbank.practicum.kafka.dto.RadiatorCommandMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaCommandProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBlindsCommand(BlindsCommandMessage message) {
        kafkaTemplate.send("blinds.commands", message.deviceId().toString(), message);
        log.info("Sent blinds command to Kafka: {}", message);
    }

    public void sendRadiatorCommand(RadiatorCommandMessage message) {
        kafkaTemplate.send("radiator.commands", message.deviceId().toString(), message);
        log.info("Sent radiator command to Kafka: {}", message);
    }
}
