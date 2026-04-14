package ru.tbank.practicum.controller.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.Schedule;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.service.BlindsService;
import ru.tbank.practicum.service.ScheduleService;

@WebMvcTest(BlindsController.class)
public class BlindsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlindsService blindsService;

    @MockitoBean
    private ScheduleService scheduleService;

    @DisplayName("Должен вернуть страницу со всеми Blinds")
    @Test
    void shouldReturnBlindsIndexPage() throws Exception {
        when(blindsService.getAll()).thenReturn(List.of(new Blinds(), new Blinds()));

        mockMvc.perform(get("/blinds"))
                .andExpect(status().isOk())
                .andExpect(view().name("blinds/index"))
                .andExpect(model().attributeExists("blindsList"));

        verify(blindsService).getAll();
    }

    @DisplayName("Должен вернуть страницу создания Blinds")
    @Test
    void shouldReturnBlindsNewPage() throws Exception {

        mockMvc.perform(get("/blinds/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("blinds/new"))
                .andExpect(model().attributeExists("blinds"));
    }

    @DisplayName("Должен сохранить Blinds")
    @Test
    void shouldRedirectAndSaveBlinds() throws Exception {
        mockMvc.perform(post("/blinds")
                        .param("state", "OPEN")
                        .param("isOnline", "true")
                        .param("isBroken", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/blinds"));

        verify(blindsService).save(any(Blinds.class));
    }

    @DisplayName("Должен изменить Blinds")
    @Test
    void shouldEditBlinds() throws Exception {
        Long id = 1L;
        Blinds blinds = new Blinds();
        blinds.setId(id);

        when(blindsService.getById(id)).thenReturn(blinds);
        when(scheduleService.getByBlindsId(id)).thenReturn(List.of(new Schedule()));

        mockMvc.perform(get("/blinds/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("blinds/edit"))
                .andExpect(model().attributeExists("blinds"))
                .andExpect(model().attributeExists("schedules"))
                .andExpect(model().attributeExists("newSchedule"));

        verify(blindsService).getById(id);
        verify(scheduleService).getByBlindsId(id);
    }

    @DisplayName("Должен добавить правило Schedule")
    @Test
    void shouldAddRule() throws Exception {
        Long blindsId = 1L;
        Blinds blinds = new Blinds();
        blinds.setId(blindsId);

        when(blindsService.getById(blindsId)).thenReturn(blinds);

        mockMvc.perform(post("/blinds/1/schedules")
                        .param("openAt", "08:00")
                        .param("closeAt", "09:00")
                        .param("enabled", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/blinds/1/edit"));

        verify(blindsService).getById(blindsId);
        verify(scheduleService).save(any(Schedule.class));
    }

    @DisplayName("Должен удалить правило Schedule")
    @Test
    void shouldDeleteRule() throws Exception {

        mockMvc.perform(post("/blinds/1/schedules/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/blinds/1/edit"));

        verify(scheduleService).deleteById(2L, EventSource.USER);
    }

    @DisplayName("Должен обновить Blinds")
    @Test
    void shouldUpdateBlinds() throws Exception {

        mockMvc.perform(post("/blinds/1")
                        .param("state", "OPEN")
                        .param("isOnline", "true")
                        .param("isBroken", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/blinds"));

        verify(blindsService).updateFromForm(eq(1L), any(Blinds.class), eq(EventSource.USER));
    }

    @DisplayName("Должен удалить Blinds")
    @Test
    void shouldDeleteBlinds() throws Exception {
        mockMvc.perform(post("/blinds/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/blinds"));

        verify(blindsService).deleteById(1L);
    }
}
