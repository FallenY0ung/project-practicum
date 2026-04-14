package ru.tbank.practicum.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.Schedule;
import ru.tbank.practicum.enums.DeviceType;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.enums.LogStatus;
import ru.tbank.practicum.repositories.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private ScheduleService scheduleService;

    @DisplayName("Должен вернуть schedule по id")
    @Test
    void shouldReturnScheduleById() {
        Schedule schedule = createSchedule(1L, 10L, LocalTime.of(8, 0), LocalTime.of(20, 0), true);

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        Schedule result = scheduleService.getById(1L);

        assertSame(schedule, result);
        assertEquals(1L, result.getId());

        verify(scheduleRepository).findById(1L);
    }

    @DisplayName("Должен вернуть все schedules")
    @Test
    void shouldReturnAllSchedules() {
        Schedule schedule1 = createSchedule(1L, 10L, LocalTime.of(8, 0), LocalTime.of(20, 0), true);

        Schedule schedule2 = createSchedule(2L, 10L, LocalTime.of(9, 0), LocalTime.of(21, 0), false);

        List<Schedule> schedules = List.of(schedule1, schedule2);

        when(scheduleRepository.findAll()).thenReturn(schedules);

        List<Schedule> result = scheduleService.getAll();

        assertSame(schedules, result);
        assertEquals(2, result.size());

        verify(scheduleRepository).findAll();
    }

    @DisplayName("Должен вернуть активные schedules по blindsId")
    @Test
    void shouldReturnActiveSchedulesByBlindsId() {
        Long blindsId = 10L;

        Schedule schedule1 = createSchedule(1L, blindsId, LocalTime.of(8, 0), LocalTime.of(20, 0), true);

        Schedule schedule2 = createSchedule(2L, blindsId, LocalTime.of(9, 0), LocalTime.of(21, 0), true);

        List<Schedule> schedules = List.of(schedule1, schedule2);

        when(scheduleRepository.findByBlindsIdAndEnabledTrue(blindsId)).thenReturn(schedules);

        List<Schedule> result = scheduleService.getActiveByBlindsId(blindsId);

        assertSame(schedules, result);
        assertEquals(2, result.size());

        verify(scheduleRepository).findByBlindsIdAndEnabledTrue(blindsId);
    }

    @DisplayName("Должен вернуть все schedules по blindsId")
    @Test
    void shouldReturnSchedulesByBlindsId() {
        Long blindsId = 10L;

        Schedule schedule1 = createSchedule(1L, blindsId, LocalTime.of(8, 0), LocalTime.of(20, 0), true);

        Schedule schedule2 = createSchedule(2L, blindsId, LocalTime.of(9, 0), LocalTime.of(21, 0), false);

        List<Schedule> schedules = List.of(schedule1, schedule2);

        when(scheduleRepository.findByBlindsId(blindsId)).thenReturn(schedules);

        List<Schedule> result = scheduleService.getByBlindsId(blindsId);

        assertSame(schedules, result);
        assertEquals(2, result.size());

        verify(scheduleRepository).findByBlindsId(blindsId);
    }

    @DisplayName("Должен удалить schedule и создать лог")
    @Test
    void shouldDeleteScheduleById() {
        Long id = 1L;
        EventSource source = EventSource.USER;

        Schedule schedule = createSchedule(id, 10L, LocalTime.of(8, 0), LocalTime.of(20, 0), true);

        when(scheduleRepository.findById(id)).thenReturn(Optional.of(schedule));

        scheduleService.deleteById(id, source);

        verify(scheduleRepository).findById(id);
        verify(logService)
                .createLog(
                        DeviceType.RADIATOR,
                        schedule.getId(),
                        LogStatus.WARNING,
                        source,
                        "DELETE_RADIATOR_RULE",
                        "Radiator rule was deleted");
        verify(scheduleRepository).delete(schedule);
    }

    @DisplayName("Должен сохранить schedule и создать лог")
    @Test
    void shouldSaveSchedule() {
        Schedule schedule = createSchedule(null, 10L, LocalTime.of(8, 0), LocalTime.of(20, 0), true);

        Schedule savedSchedule = createSchedule(1L, 10L, LocalTime.of(8, 0), LocalTime.of(20, 0), true);

        when(scheduleRepository.save(schedule)).thenReturn(savedSchedule);

        Schedule result = scheduleService.save(schedule);

        assertSame(savedSchedule, result);
        assertEquals(1L, result.getId());

        verify(scheduleRepository).save(schedule);
        verify(logService)
                .createLog(
                        DeviceType.BLINDS,
                        10L,
                        LogStatus.SUCCESS,
                        EventSource.SYSTEM,
                        "CREATE_SCHEDULE",
                        "Schedule was created successfully");
    }

    @DisplayName("Должен обновить schedule и создать лог")
    @Test
    void shouldUpdateSchedule() {
        Long id = 1L;

        Schedule schedule = createSchedule(id, 10L, LocalTime.of(8, 0), LocalTime.of(20, 0), true);

        LocalTime newOpenAt = LocalTime.of(9, 0);
        LocalTime newCloseAt = LocalTime.of(21, 0);
        Boolean newEnabled = false;

        when(scheduleRepository.findById(id)).thenReturn(Optional.of(schedule));

        Schedule result = scheduleService.updateSchedule(id, newOpenAt, newCloseAt, newEnabled);

        assertSame(schedule, result);
        assertEquals(newOpenAt, result.getOpenAt());
        assertEquals(newCloseAt, result.getCloseAt());
        assertEquals(newEnabled, result.getEnabled());

        verify(scheduleRepository).findById(id);
        verify(logService)
                .createLog(
                        DeviceType.BLINDS,
                        10L,
                        LogStatus.SUCCESS,
                        EventSource.SYSTEM,
                        "UPDATE_SCHEDULE",
                        "Schedule was updated successfully");
    }

    private Schedule createSchedule(
            Long scheduleId, Long blindsId, LocalTime openAt, LocalTime closeAt, Boolean enabled) {
        Blinds blinds = new Blinds();
        blinds.setId(blindsId);

        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        schedule.setBlinds(blinds);
        schedule.setOpenAt(openAt);
        schedule.setCloseAt(closeAt);
        schedule.setEnabled(enabled);

        return schedule;
    }
}
