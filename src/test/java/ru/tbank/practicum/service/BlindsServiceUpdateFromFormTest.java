package ru.tbank.practicum.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.enums.BlindsState;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.repositories.BlindsRepository;
import ru.tbank.practicum.repositories.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class BlindsServiceUpdateFromFormTest {

    @Mock
    private BlindsRepository blindsRepository;

    @Mock
    private DeviceEventService deviceEventService;

    @Mock
    private LogService logService;

    @Mock
    private ScheduleRepository scheduleRepository;

    private BlindsService blindsService;

    @BeforeEach
    void setUp() {
        blindsService = spy(new BlindsService(blindsRepository, deviceEventService, logService, scheduleRepository));
    }

    @DisplayName("Должен ничего не менять, если форма не содержит изменений")
    @Test
    void shouldReturnSameBlindsWhenNothingChanged() {
        Long id = 1L;
        EventSource source = EventSource.USER;

        Blinds current = createBlinds(id, BlindsState.OPEN, true, false);
        Blinds updated = createBlinds(id, BlindsState.OPEN, true, false);

        doReturn(current, current).when(blindsService).getById(id);

        Blinds result = blindsService.updateFromForm(id, updated, source);

        assertSame(current, result);

        verify(blindsService, never()).updateState(anyLong(), any(), any());
        verify(blindsService, never()).markAsBroken(anyLong(), any());
        verify(blindsService, never()).restore(anyLong(), any());
        verify(blindsService, times(2)).getById(id);
    }

    @DisplayName("Должен вызвать updateState, если изменилось состояние")
    @Test
    void shouldCallUpdateStateWhenStateChanged() {
        Long id = 1L;
        EventSource source = EventSource.USER;

        Blinds current = createBlinds(id, BlindsState.OPEN, true, false);
        Blinds updated = createBlinds(id, BlindsState.CLOSED, true, false);
        Blinds resultAfterUpdate = createBlinds(id, BlindsState.CLOSED, true, false);

        doReturn(current, resultAfterUpdate).when(blindsService).getById(id);
        doReturn(resultAfterUpdate).when(blindsService).updateState(id, BlindsState.CLOSED, source);

        Blinds result = blindsService.updateFromForm(id, updated, source);

        assertSame(resultAfterUpdate, result);

        verify(blindsService).updateState(id, BlindsState.CLOSED, source);
        verify(blindsService, never()).changeOnlineStatus(anyLong(), anyBoolean(), any());
        verify(blindsService, never()).markAsBroken(anyLong(), any());
        verify(blindsService, never()).restore(anyLong(), any());
    }

    @DisplayName("Должен вызвать changeOnlineStatus, если изменился статус online")
    @Test
    void shouldCallChangeOnlineStatusWhenOnlineChanged() {
        Long id = 1L;
        EventSource source = EventSource.USER;

        Blinds current = createBlinds(id, BlindsState.OPEN, true, false);
        Blinds updated = createBlinds(id, BlindsState.OPEN, false, false);
        Blinds resultAfterUpdate = createBlinds(id, BlindsState.OPEN, false, false);

        doReturn(current, resultAfterUpdate).when(blindsService).getById(id);
        doReturn(resultAfterUpdate).when(blindsService).changeOnlineStatus(id, false, source);

        Blinds result = blindsService.updateFromForm(id, updated, source);

        assertSame(resultAfterUpdate, result);

        verify(blindsService).changeOnlineStatus(id, false, source);
        verify(blindsService, never()).updateState(anyLong(), any(), any());
        verify(blindsService, never()).markAsBroken(anyLong(), any());
        verify(blindsService, never()).restore(anyLong(), any());
    }

    @DisplayName("Должен вызвать markAsBroken, если isBroken изменился на true")
    @Test
    void shouldCallMarkAsBrokenWhenBrokenChangedToTrue() {
        Long id = 1L;
        EventSource source = EventSource.USER;

        Blinds current = createBlinds(id, BlindsState.OPEN, true, false);
        Blinds updated = createBlinds(id, BlindsState.OPEN, true, true);
        Blinds resultAfterUpdate = createBlinds(id, BlindsState.OPEN, true, true);

        doReturn(current, resultAfterUpdate).when(blindsService).getById(id);
        doReturn(resultAfterUpdate).when(blindsService).markAsBroken(id, source);

        Blinds result = blindsService.updateFromForm(id, updated, source);

        assertSame(resultAfterUpdate, result);

        verify(blindsService).markAsBroken(id, source);
        verify(blindsService, never()).restore(anyLong(), any());
        verify(blindsService, never()).updateState(anyLong(), any(), any());
        verify(blindsService, never()).changeOnlineStatus(anyLong(), anyBoolean(), any());
    }

    @DisplayName("Должен вызвать restore, если isBroken изменился на false")
    @Test
    void shouldCallRestoreWhenBrokenChangedToFalse() {
        Long id = 1L;
        EventSource source = EventSource.USER;

        Blinds current = createBlinds(id, BlindsState.OPEN, true, true);
        Blinds updated = createBlinds(id, BlindsState.OPEN, true, false);
        Blinds resultAfterUpdate = createBlinds(id, BlindsState.OPEN, true, false);

        doReturn(current, resultAfterUpdate).when(blindsService).getById(id);
        doReturn(resultAfterUpdate).when(blindsService).restore(id, source);

        Blinds result = blindsService.updateFromForm(id, updated, source);

        assertSame(resultAfterUpdate, result);

        verify(blindsService).restore(id, source);
        verify(blindsService, never()).markAsBroken(anyLong(), any());
        verify(blindsService, never()).updateState(anyLong(), any(), any());
        verify(blindsService, never()).changeOnlineStatus(anyLong(), anyBoolean(), any());
    }

    @DisplayName("Должен вызвать все нужные методы, если изменилось несколько полей")
    @Test
    void shouldCallAllRequiredMethodsWhenSeveralFieldsChanged() {
        Long id = 1L;
        EventSource source = EventSource.USER;

        Blinds current = createBlinds(id, BlindsState.OPEN, true, false);
        Blinds updated = createBlinds(id, BlindsState.CLOSED, false, true);
        Blinds resultAfterUpdate = createBlinds(id, BlindsState.CLOSED, false, true);

        doReturn(current, resultAfterUpdate).when(blindsService).getById(id);
        doReturn(resultAfterUpdate).when(blindsService).updateState(id, BlindsState.CLOSED, source);
        doReturn(resultAfterUpdate).when(blindsService).changeOnlineStatus(id, false, source);
        doReturn(resultAfterUpdate).when(blindsService).markAsBroken(id, source);

        Blinds result = blindsService.updateFromForm(id, updated, source);

        assertSame(resultAfterUpdate, result);

        verify(blindsService).updateState(id, BlindsState.CLOSED, source);
        verify(blindsService).changeOnlineStatus(id, false, source);
        verify(blindsService).markAsBroken(id, source);
        verify(blindsService, never()).restore(anyLong(), any());
    }

    private Blinds createBlinds(Long id, BlindsState state, Boolean isOnline, Boolean isBroken) {
        Blinds blinds = new Blinds();
        blinds.setId(id);
        blinds.setState(state);
        blinds.setIsOnline(isOnline);
        blinds.setIsBroken(isBroken);
        return blinds;
    }
}
