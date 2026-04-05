package ru.tbank.practicum.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.Schedule;
import ru.tbank.practicum.enums.BlindsState;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.EventType;
import ru.tbank.practicum.enums.LogStatus;
import ru.tbank.practicum.repositories.BlindsRepository;
import ru.tbank.practicum.repositories.ScheduleRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BlindsServiceTest {

    @Mock
    private BlindsRepository blindsRepository;

    @Mock
    private DeviceEventService deviceEventService;

    @Mock
    private LogService logService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private BlindsService blindsService;

    @DisplayName("Должен вызвать внутри себя blindsRepository.findAll и вернуть List")
    @Test
    public void shouldReturnAllBlinds() {
        List<Blinds> blindsList = List.of(new Blinds(), new Blinds());

        when(blindsRepository.findAll()).thenReturn(blindsList);

        List<Blinds> result = blindsService.getAll();

        assertEquals(blindsList, result);
        verify(blindsRepository).findAll();
    }

    @DisplayName("Должен вызвать внутри себя blindsRepository.findByBlindsId и вернуть List")
    @Test
    public void shouldReturnByBlindsId() {
        List<Schedule> scheduleList = List.of(new Schedule(), new Schedule());

        when(scheduleRepository.findByBlindsId(1L)).thenReturn(scheduleList);

        List<Schedule> result = blindsService.getByBlindsId(1L);

        assertEquals(scheduleList, result);
        verify(scheduleRepository).findByBlindsId(1L);
    }

    @DisplayName("Должен вызвать внутри себя blindsRepository.findById и вернуть Blind")
    @Test
    public void shouldReturnBlindById() {
        Blinds blinds = new Blinds();

        when(blindsRepository.findById(1L)).thenReturn(Optional.of(blinds));

        Blinds result = blindsService.getById(1L);

        assertEquals(blinds, result);
        verify(blindsRepository).findById(1L);

    }

    @DisplayName("Должен сохранить Blinds и создать Log через LogService")
    @Test
    public void shouldSaveBlindsAndCreateLog() {
        Blinds blinds = new Blinds();

        Blinds saved = new Blinds();
        saved.setId(1L);
        saved.setState(BlindsState.OPEN);

        when(blindsRepository.save(blinds)).thenReturn(saved);
        Blinds result = blindsService.save(blinds);

        assertEquals(saved, result);

        verify(blindsRepository).save(blinds);
        verify(logService).createLog(
                DeviceType.BLINDS,
                1L,
                LogStatus.SUCCESS,
                EventSource.SYSTEM,
                "CREATE_BLINDS",
                "Blinds were created successfully"
        );
    }

    @DisplayName("Должен удалить и создать Log")
    @Test
    public void shouldDeleteById(){
        Blinds blinds = new Blinds();
        blinds.setId(1L);

        when(blindsRepository.findById(1L)).thenReturn(Optional.of(blinds));
        when(scheduleRepository.findByBlindsIdAndEnabledTrue(1L)).thenReturn(Collections.emptyList());

        blindsService.deleteById(1L);

        verify(blindsRepository).delete(blinds);
        verify(logService).createLog(
                DeviceType.BLINDS,
                blinds.getId(),
                LogStatus.WARNING,
                EventSource.USER,
                "DELETE_BLINDS",
                "Blinds were deleted"
        );
    }

    @DisplayName("Должен обновить и создать Log и Event")
    @Test
    public void shouldUpdateState(){
        Blinds blinds = new Blinds();
        blinds.setId(1L);
        blinds.setState(BlindsState.OPEN);
        BlindsState newState = BlindsState.CLOSED;
        BlindsState oldState = blinds.getState();
        EventSource source = EventSource.USER;

        when(blindsRepository.findById(1L)).thenReturn(Optional.of(blinds));

        Blinds result = blindsService.updateState(1L, newState, source);

        assertEquals(newState, result.getState());
        verify(blindsRepository).findById(1L);

        verify(deviceEventService).createEvent(
                eq(DeviceType.BLINDS),
                eq(blinds.getId()),
                any(),
                eq(source),
                eq("Blinds state changed from " + oldState + " to " + newState)
        );

        verify(logService).createLog(
                DeviceType.BLINDS,
                blinds.getId(),
                LogStatus.SUCCESS,
                source,
                "UPDATE_BLINDS_STATE",
                "Blinds state changed from " + oldState + " to " + newState
        );
    }

    @DisplayName("Должен сменить статус online")
    @Test
    public void shouldChangeOnlineStatus(){
        Blinds blinds = new Blinds();
        blinds.setId(1L);
        blinds.setIsOnline(false);
        boolean online = true;
        EventSource source = EventSource.USER;

        when(blindsRepository.findById(1L)).thenReturn(Optional.of(blinds));

        Blinds result = blindsService.changeOnlineStatus(1L, online, source);

        assertEquals(online, result.getIsOnline());
        verify(blindsRepository).findById(1L);

        verify(deviceEventService).createEvent(
                DeviceType.BLINDS,
                blinds.getId(),
                online ? EventType.BLINDS_ONLINE : EventType.BLINDS_OFFLINE,
                source,
                "Blinds online status changed to " + online
        );

        verify(logService).createLog(
                DeviceType.BLINDS,
                blinds.getId(),
                LogStatus.SUCCESS,
                source,
                "CHANGE_BLINDS_ONLINE_STATUS",
                "Blinds online status changed to " + online
        );
    }

    @DisplayName("Должен отметить как сломанный")
    @Test
    public void shouldMarkAsBroken(){
        Blinds blinds = new Blinds();
        blinds.setId(1L);
        blinds.setIsBroken(false);
        EventSource source = EventSource.USER;

        when(blindsRepository.findById(1L)).thenReturn(Optional.of(blinds));

        Blinds result = blindsService.markAsBroken(1L, source);

        assertEquals(result.getIsBroken(), true);
        verify(blindsRepository).findById(1L);

        verify(deviceEventService).createEvent(
                DeviceType.BLINDS,
                blinds.getId(),
                EventType.BLINDS_BROKEN,
                source,
                "Blinds marked as broken"
        );

        verify(logService).createLog(
                DeviceType.BLINDS,
                blinds.getId(),
                LogStatus.WARNING,
                source,
                "MARK_BLINDS_BROKEN",
                "Blinds marked as broken"
        );
    }

    @DisplayName("Должен отметить как НЕсломанный")
    @Test
    public void shouldRestore(){
        Blinds blinds = new Blinds();
        blinds.setId(1L);
        blinds.setIsBroken(true);
        EventSource source = EventSource.USER;

        when(blindsRepository.findById(1L)).thenReturn(Optional.of(blinds));

        Blinds result = blindsService.restore(1L, source);

        assertEquals(result.getIsBroken(), false);
        verify(blindsRepository).findById(1L);

        verify(deviceEventService).createEvent(
                DeviceType.BLINDS,
                blinds.getId(),
                EventType.BLINDS_RESTORED,
                source,
                "Blinds restored"
        );

        verify(logService).createLog(
                DeviceType.BLINDS,
                blinds.getId(),
                LogStatus.SUCCESS,
                source,
                "RESTORE_BLINDS",
                "Blinds restored successfully"
        );
    }

    @DisplayName("Должен преобразовывать BlindsState to EventType")
    @ParameterizedTest
    @CsvSource({
            "OPEN, BLINDS_OPENED",
            "CLOSED, BLINDS_CLOSED",
            "HALF_OPEN, BLINDS_HALF_OPENED"
    })
    public void shouldMapStateToEventType(BlindsState state, EventType expected){
        EventType actual = blindsService.mapStateToEventType(state);
        assertEquals(expected, actual);
    }














}
