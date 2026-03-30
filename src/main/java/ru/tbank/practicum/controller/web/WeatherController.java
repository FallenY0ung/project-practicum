package ru.tbank.practicum.controller.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tbank.practicum.dto.WeatherProperties;
import ru.tbank.practicum.entity.Weather;
import ru.tbank.practicum.service.WeatherService;
import ru.tbank.practicum.service.WeatherSyncService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherSyncService weatherSyncService;
    private final WeatherProperties weatherProperties;

    @GetMapping
    public String getWeatherPage(Model model) {
        Weather latestWeather = null;
        try {
            latestWeather = weatherService.getLatest();
        } catch (Exception e) {
            log.warn("Failed to load latest weather", e);
        }

        model.addAttribute("latestWeather", latestWeather);
        model.addAttribute("weatherHistory", weatherService.getAll());
        model.addAttribute("city", weatherProperties.city());

        return "weather/index";
    }

    @PostMapping("/sync")
    public String syncWeather() {
        weatherSyncService.fetchAndSaveCurrentWeather(weatherProperties.city());
        return "redirect:/weather";
    }
}
