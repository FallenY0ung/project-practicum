package ru.tbank.practicum.controller.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tbank.practicum.dto.WeatherProperties;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.Radiator;
import ru.tbank.practicum.entity.RadiatorRule;
import ru.tbank.practicum.entity.Schedule;
import ru.tbank.practicum.entity.Weather;
import ru.tbank.practicum.service.BlindsService;
import ru.tbank.practicum.service.RadiatorRuleService;
import ru.tbank.practicum.service.RadiatorService;
import ru.tbank.practicum.service.ScheduleService;
import ru.tbank.practicum.service.SmartHomeService;
import ru.tbank.practicum.service.WeatherAdviceService;
import ru.tbank.practicum.service.WeatherService;
import ru.tbank.practicum.service.WeatherSyncService;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RadiatorService radiatorService;

    @MockitoBean
    private BlindsService blindsService;

    @MockitoBean
    private RadiatorRuleService radiatorRuleService;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private WeatherSyncService weatherSyncService;

    @MockitoBean
    private SmartHomeService smartHomeService;

    @MockitoBean
    private WeatherProperties weatherProperties;

    @MockitoBean
    private WeatherAdviceService weatherAdviceService;

    @DisplayName("Должен открыть dashboard и положить данные в model")
    @Test
    void shouldReturnDashboardPage() throws Exception {
        Weather weather = new Weather();

        when(weatherService.getLatest()).thenReturn(weather);
        when(weatherAdviceService.getAdvice(weather)).thenReturn("Надень куртку.");
        when(radiatorService.getAll()).thenReturn(List.of(new Radiator(), new Radiator()));
        when(blindsService.getAll()).thenReturn(List.of(new Blinds()));
        when(radiatorRuleService.getAll())
                .thenReturn(List.of(new RadiatorRule(), new RadiatorRule(), new RadiatorRule()));
        when(scheduleService.getAll())
                .thenReturn(List.of(new Schedule(), new Schedule(), new Schedule(), new Schedule()));
        when(weatherService.getAll())
                .thenReturn(List.of(new Weather(), new Weather(), new Weather(), new Weather(), new Weather()));
        when(weatherProperties.city()).thenReturn("Moscow");

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attribute("radiatorsCount", 2))
                .andExpect(model().attribute("blindsCount", 1))
                .andExpect(model().attribute("rulesCount", 3))
                .andExpect(model().attribute("schedulesCount", 4))
                .andExpect(model().attribute("weatherCount", 5))
                .andExpect(model().attribute("latestWeather", weather))
                .andExpect(model().attribute("aiAdvice", "Надень куртку."))
                .andExpect(model().attribute("city", "Moscow"));

        verify(weatherService).getLatest();
        verify(weatherAdviceService).getAdvice(weather);
        verify(radiatorService).getAll();
        verify(blindsService).getAll();
        verify(radiatorRuleService).getAll();
        verify(scheduleService).getAll();
        verify(weatherService).getAll();
        verify(weatherProperties).city();
    }

    @DisplayName("Должен открыть dashboard без aiAdvice, если погода не загрузилась")
    @Test
    void shouldReturnDashboardPageWithoutAiAdviceWhenWeatherLoadingFailed() throws Exception {
        when(weatherService.getLatest()).thenThrow(new RuntimeException("Weather not available"));
        when(radiatorService.getAll()).thenReturn(List.of(new Radiator(), new Radiator()));
        when(blindsService.getAll()).thenReturn(List.of(new Blinds()));
        when(radiatorRuleService.getAll())
                .thenReturn(List.of(new RadiatorRule(), new RadiatorRule(), new RadiatorRule()));
        when(scheduleService.getAll())
                .thenReturn(List.of(new Schedule(), new Schedule(), new Schedule(), new Schedule()));
        when(weatherService.getAll())
                .thenReturn(List.of(new Weather(), new Weather(), new Weather(), new Weather(), new Weather()));
        when(weatherProperties.city()).thenReturn("Moscow");

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attribute("radiatorsCount", 2))
                .andExpect(model().attribute("blindsCount", 1))
                .andExpect(model().attribute("rulesCount", 3))
                .andExpect(model().attribute("schedulesCount", 4))
                .andExpect(model().attribute("weatherCount", 5))
                .andExpect(model().attribute("latestWeather", (Object) null))
                .andExpect(model().attribute("aiAdvice", (Object) null))
                .andExpect(model().attribute("city", "Moscow"));

        verify(weatherService).getLatest();
        verify(radiatorService).getAll();
        verify(blindsService).getAll();
        verify(radiatorRuleService).getAll();
        verify(scheduleService).getAll();
        verify(weatherService).getAll();
        verify(weatherProperties).city();
    }

    @DisplayName("Должен открыть dashboard и показать сообщение об ошибке, если aiAdvice не получен")
    @Test
    void shouldReturnDashboardPageWithFallbackAiAdviceWhenAdviceRequestFailed() throws Exception {
        Weather weather = new Weather();

        when(weatherService.getLatest()).thenReturn(weather);
        when(weatherAdviceService.getAdvice(any(Weather.class))).thenThrow(new RuntimeException("OpenRouter error"));
        when(radiatorService.getAll()).thenReturn(List.of(new Radiator(), new Radiator()));
        when(blindsService.getAll()).thenReturn(List.of(new Blinds()));
        when(radiatorRuleService.getAll())
                .thenReturn(List.of(new RadiatorRule(), new RadiatorRule(), new RadiatorRule()));
        when(scheduleService.getAll())
                .thenReturn(List.of(new Schedule(), new Schedule(), new Schedule(), new Schedule()));
        when(weatherService.getAll())
                .thenReturn(List.of(new Weather(), new Weather(), new Weather(), new Weather(), new Weather()));
        when(weatherProperties.city()).thenReturn("Moscow");

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attribute("radiatorsCount", 2))
                .andExpect(model().attribute("blindsCount", 1))
                .andExpect(model().attribute("rulesCount", 3))
                .andExpect(model().attribute("schedulesCount", 4))
                .andExpect(model().attribute("weatherCount", 5))
                .andExpect(model().attribute("latestWeather", weather))
                .andExpect(model().attribute("aiAdvice", "Не удалось получить рекомендацию от ИИ."))
                .andExpect(model().attribute("city", "Moscow"));

        verify(weatherService).getLatest();
        verify(weatherAdviceService).getAdvice(weather);
        verify(radiatorService).getAll();
        verify(blindsService).getAll();
        verify(radiatorRuleService).getAll();
        verify(scheduleService).getAll();
        verify(weatherService).getAll();
        verify(weatherProperties).city();
    }

    @DisplayName("Должен синхронизировать погоду и сделать redirect на dashboard")
    @Test
    void shouldSyncWeatherAndRedirect() throws Exception {
        when(weatherProperties.city()).thenReturn("Moscow");

        mockMvc.perform(post("/dashboard/weather/sync"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("successMessage", "Погода успешно обновлена."));

        verify(weatherProperties).city();
        verify(weatherSyncService).fetchAndSaveCurrentWeather("Moscow");
    }

    @DisplayName("Должен применить правила радиаторов и сделать redirect на dashboard")
    @Test
    void shouldSyncRadiatorsAndRedirect() throws Exception {
        mockMvc.perform(post("/dashboard/sync/radiators"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("successMessage", "Правила радиаторов успешно применены."));

        verify(smartHomeService).applyWeatherRulesToAllRadiators();
    }

    @DisplayName("Должен применить расписания жалюзи и сделать redirect на dashboard")
    @Test
    void shouldSyncBlindsAndRedirect() throws Exception {
        mockMvc.perform(post("/dashboard/sync/blinds"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("successMessage", "Расписания жалюзи успешно применены."));

        verify(smartHomeService).applySchedulesToAllBlinds();
    }

    @DisplayName("Должен выполнить полную синхронизацию и сделать redirect на dashboard")
    @Test
    void shouldSyncAllAndRedirect() throws Exception {
        mockMvc.perform(post("/dashboard/sync/all"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("successMessage", "Полная синхронизация умного дома завершена."));

        verify(smartHomeService).syncAllDevices();
    }
}
