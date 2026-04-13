package ru.tbank.practicum.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.DeviceEvent;
import ru.tbank.practicum.entity.Log;
import ru.tbank.practicum.enums.BlindsState;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.EventType;
import ru.tbank.practicum.enums.LogStatus;
import ru.tbank.practicum.repositories.BlindsRepository;

@Tag("integration")
@Testcontainers
@DataJpaTest(properties = {"spring.liquibase.enabled=true"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BlindsService.class, DeviceEventService.class, LogService.class})
class BlindsServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"));

    @Autowired
    private BlindsService blindsService;

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private LogService logService;

    @Autowired
    private BlindsRepository blindsRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("updateState должен обновить blinds и создать DeviceEvent + Log в реальной БД")
    void updateState_shouldPersistNewState_andCreateEventAndLog() {
        Blinds blinds = Blinds.builder()
                .state(BlindsState.OPEN)
                .isOnline(true)
                .isBroken(false)
                .build();

        Blinds saved = blindsRepository.saveAndFlush(blinds);

        Blinds updated = blindsService.updateState(saved.getId(), BlindsState.CLOSED, EventSource.USER);

        entityManager.flush();
        entityManager.clear();

        Blinds reloaded = blindsRepository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getState()).isEqualTo(BlindsState.CLOSED);
        assertThat(reloaded.getIsOnline()).isTrue();
        assertThat(reloaded.getIsBroken()).isFalse();

        List<DeviceEvent> events = deviceEventService.getByDevice(DeviceType.BLINDS, saved.getId());
        assertThat(events).hasSize(1);

        DeviceEvent event = events.get(0);
        assertThat(event.getDeviceType()).isEqualTo(DeviceType.BLINDS);
        assertThat(event.getDeviceId()).isEqualTo(saved.getId());
        assertThat(event.getEventType()).isEqualTo(EventType.BLINDS_CLOSED);
        assertThat(event.getSource()).isEqualTo(EventSource.USER);
        assertThat(event.getMessage()).contains("OPEN").contains("CLOSED");

        List<Log> logs = logService.getByDevice(DeviceType.BLINDS, saved.getId());
        assertThat(logs).hasSize(1);

        Log log = logs.get(0);
        assertThat(log.getDeviceType()).isEqualTo(DeviceType.BLINDS);
        assertThat(log.getDeviceId()).isEqualTo(saved.getId());
        assertThat(log.getStatus()).isEqualTo(LogStatus.SUCCESS);
        assertThat(log.getSource()).isEqualTo(EventSource.USER);
        assertThat(log.getAction()).isEqualTo("UPDATE_BLINDS_STATE");
        assertThat(log.getMessage()).contains("OPEN").contains("CLOSED");
    }

    @Test
    @DisplayName("updateState не должен создавать DeviceEvent и Log, если состояние не изменилось")
    void updateState_shouldDoNothing_whenStateIsSame() {
        Blinds blinds = Blinds.builder()
                .state(BlindsState.OPEN)
                .isOnline(true)
                .isBroken(false)
                .build();

        Blinds saved = blindsRepository.saveAndFlush(blinds);

        Blinds result = blindsService.updateState(saved.getId(), BlindsState.OPEN, EventSource.USER);

        entityManager.flush();
        entityManager.clear();

        Blinds reloaded = blindsRepository.findById(saved.getId()).orElseThrow();

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getState()).isEqualTo(BlindsState.OPEN);

        List<DeviceEvent> events = deviceEventService.getByDevice(DeviceType.BLINDS, saved.getId());
        assertThat(events).isEmpty();

        List<Log> logs = logService.getByDevice(DeviceType.BLINDS, saved.getId());
        assertThat(logs).isEmpty();
    }
}
