package ru.tbank.practicum.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.kafka.dto.DeviceEventMessage;
import ru.tbank.practicum.service.BlindsService;
import ru.tbank.practicum.service.RadiatorService;

@ExtendWith(MockitoExtension.class)
class KafkaDeviceEventListenerTest {

    @Mock
    private BlindsService blindsService;

    @Mock
    private RadiatorService radiatorService;

    @InjectMocks
    private KafkaDeviceEventListener kafkaDeviceEventListener;

    @Test
    @DisplayName("listen должен вызвать markAsBroken для штор при событии BROKEN")
    void listen_shouldCallBlindsMarkAsBroken_whenBlindsBrokenEventReceived() {
        DeviceEventMessage message = mockMessage(DeviceType.BLINDS, "BROKEN", 1L);

        kafkaDeviceEventListener.listen(message);

        verify(blindsService).markAsBroken(1L, EventSource.SYSTEM);
        verifyNoInteractions(radiatorService);
        verifyNoMoreInteractions(blindsService);
    }

    @Test
    @DisplayName("listen должен перевести шторы в offline при событии OFFLINE")
    void listen_shouldCallBlindsChangeOnlineStatusFalse_whenBlindsOfflineEventReceived() {
        DeviceEventMessage message = mockMessage(DeviceType.BLINDS, "OFFLINE", 2L);

        kafkaDeviceEventListener.listen(message);

        verify(blindsService).changeOnlineStatus(2L, false, EventSource.SYSTEM);
        verifyNoInteractions(radiatorService);
        verifyNoMoreInteractions(blindsService);
    }

    @Test
    @DisplayName("listen должен перевести шторы в online при событии ONLINE")
    void listen_shouldCallBlindsChangeOnlineStatusTrue_whenBlindsOnlineEventReceived() {
        DeviceEventMessage message = mockMessage(DeviceType.BLINDS, "ONLINE", 3L);

        kafkaDeviceEventListener.listen(message);

        verify(blindsService).changeOnlineStatus(3L, true, EventSource.SYSTEM);
        verifyNoInteractions(radiatorService);
        verifyNoMoreInteractions(blindsService);
    }

    @Test
    @DisplayName("listen не должен вызывать сервисы при неизвестном событии штор")
    void listen_shouldDoNothing_whenUnknownBlindsEventReceived() {
        DeviceEventMessage message = mock(DeviceEventMessage.class);
        when(message.deviceType()).thenReturn(DeviceType.BLINDS);
        when(message.eventType()).thenReturn("SOMETHING_UNKNOWN");

        kafkaDeviceEventListener.listen(message);

        verifyNoInteractions(blindsService, radiatorService);
    }

    @Test
    @DisplayName("listen должен вызвать markAsBroken для радиатора при событии BROKEN")
    void listen_shouldCallRadiatorMarkAsBroken_whenRadiatorBrokenEventReceived() {
        DeviceEventMessage message = mockMessage(DeviceType.RADIATOR, "BROKEN", 10L);

        kafkaDeviceEventListener.listen(message);

        verify(radiatorService).markAsBroken(10L, EventSource.SYSTEM);
        verifyNoInteractions(blindsService);
        verifyNoMoreInteractions(radiatorService);
    }

    @Test
    @DisplayName("listen должен перевести радиатор в offline при событии OFFLINE")
    void listen_shouldCallRadiatorChangeOnlineStatusFalse_whenRadiatorOfflineEventReceived() {
        DeviceEventMessage message = mockMessage(DeviceType.RADIATOR, "OFFLINE", 11L);

        kafkaDeviceEventListener.listen(message);

        verify(radiatorService).changeOnlineStatus(11L, false, EventSource.SYSTEM);
        verifyNoInteractions(blindsService);
        verifyNoMoreInteractions(radiatorService);
    }

    @Test
    @DisplayName("listen должен перевести радиатор в online при событии ONLINE")
    void listen_shouldCallRadiatorChangeOnlineStatusTrue_whenRadiatorOnlineEventReceived() {
        DeviceEventMessage message = mockMessage(DeviceType.RADIATOR, "ONLINE", 12L);

        kafkaDeviceEventListener.listen(message);

        verify(radiatorService).changeOnlineStatus(12L, true, EventSource.SYSTEM);
        verifyNoInteractions(blindsService);
        verifyNoMoreInteractions(radiatorService);
    }

    @Test
    @DisplayName("listen не должен вызывать сервисы при неизвестном событии радиатора")
    void listen_shouldDoNothing_whenUnknownRadiatorEventReceived() {
        DeviceEventMessage message = mock(DeviceEventMessage.class);
        when(message.deviceType()).thenReturn(DeviceType.RADIATOR);
        when(message.eventType()).thenReturn("SOMETHING_UNKNOWN");

        kafkaDeviceEventListener.listen(message);

        verifyNoInteractions(blindsService, radiatorService);
    }

    @Test
    @DisplayName("listen не должен вызывать сервисы, если тип устройства не определен")
    void listen_shouldDoNothing_whenDeviceTypeIsNull() {
        DeviceEventMessage message = mock(DeviceEventMessage.class);
        when(message.deviceType()).thenReturn(null);

        kafkaDeviceEventListener.listen(message);

        verifyNoInteractions(blindsService, radiatorService);
    }

    private DeviceEventMessage mockMessage(DeviceType deviceType, String eventType, Long deviceId) {
        DeviceEventMessage message = mock(DeviceEventMessage.class);
        when(message.deviceType()).thenReturn(deviceType);
        when(message.eventType()).thenReturn(eventType);
        when(message.deviceId()).thenReturn(deviceId);
        return message;
    }
}