package ru.tbank.practicum.kafka;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.kafka.core.KafkaTemplate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.practicum.kafka.dto.BlindsCommandMessage;
import ru.tbank.practicum.kafka.dto.RadiatorCommandMessage;

@ExtendWith(MockitoExtension.class)
class KafkaCommandProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private BlindsCommandMessage blindsCommandMessage;

    @Mock
    private RadiatorCommandMessage radiatorCommandMessage;

    @InjectMocks
    private KafkaCommandProducer kafkaCommandProducer;

    @Test
    @DisplayName("sendBlindsCommand должен отправить сообщение в topic blinds.commands")
    void sendBlindsCommand_shouldSendMessageToBlindsCommandsTopic() {
        when(blindsCommandMessage.deviceId()).thenReturn(1L);

        kafkaCommandProducer.sendBlindsCommand(blindsCommandMessage);

        verify(kafkaTemplate).send("blinds.commands", "1", blindsCommandMessage);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("sendRadiatorCommand должен отправить сообщение в topic radiator.commands")
    void sendRadiatorCommand_shouldSendMessageToRadiatorCommandsTopic() {
        when(radiatorCommandMessage.deviceId()).thenReturn(2L);

        kafkaCommandProducer.sendRadiatorCommand(radiatorCommandMessage);

        verify(kafkaTemplate).send("radiator.commands", "2", radiatorCommandMessage);
        verifyNoMoreInteractions(kafkaTemplate);
    }
}