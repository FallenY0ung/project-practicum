package ru.tbank.practicum.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.tbank.practicum.dto.WeatherProperties;
import ru.tbank.practicum.entity.Weather;
import ru.tbank.practicum.service.BlindsService;
import ru.tbank.practicum.service.RadiatorRuleService;
import ru.tbank.practicum.service.RadiatorService;
import ru.tbank.practicum.service.ScheduleService;
import ru.tbank.practicum.service.SmartHomeService;
import ru.tbank.practicum.service.WeatherService;
import ru.tbank.practicum.service.WeatherSyncService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final RadiatorService radiatorService;
    private final BlindsService blindsService;
    private final RadiatorRuleService radiatorRuleService;
    private final ScheduleService scheduleService;
    private final WeatherService weatherService;
    private final WeatherSyncService weatherSyncService;
    private final SmartHomeService smartHomeService;
    private final WeatherProperties weatherProperties;

    @GetMapping
    public String dashboard(Model model) {
        Weather latestWeather = null;
        try {
            latestWeather = weatherService.getLatest();
        } catch (Exception ignored) {
        }

        model.addAttribute("radiatorsCount", radiatorService.getAll().size());
        model.addAttribute("blindsCount", blindsService.getAll().size());
        model.addAttribute("rulesCount", radiatorRuleService.getAll().size());
        model.addAttribute("schedulesCount", scheduleService.getAll().size());
        model.addAttribute("weatherCount", weatherService.getAll().size());
        model.addAttribute("latestWeather", latestWeather);
        model.addAttribute("city", weatherProperties.city());

        return "dashboard/index";
    }

    @PostMapping("/weather/sync")
    public String syncWeather(RedirectAttributes redirectAttributes) {
        weatherSyncService.fetchAndSaveCurrentWeather(weatherProperties.city());
        redirectAttributes.addFlashAttribute("successMessage", "Погода успешно обновлена.");
        return "redirect:/dashboard";
    }

    @PostMapping("/sync/radiators")
    public String syncRadiators(RedirectAttributes redirectAttributes) {
        smartHomeService.applyWeatherRulesToAllRadiators();
        redirectAttributes.addFlashAttribute("successMessage", "Правила радиаторов успешно применены.");
        return "redirect:/dashboard";
    }

    @PostMapping("/sync/blinds")
    public String syncBlinds(RedirectAttributes redirectAttributes) {
        smartHomeService.applySchedulesToAllBlinds();
        redirectAttributes.addFlashAttribute("successMessage", "Расписания жалюзи успешно применены.");
        return "redirect:/dashboard";
    }

    @PostMapping("/sync/all")
    public String syncAll(RedirectAttributes redirectAttributes) {
        smartHomeService.syncAllDevices();
        redirectAttributes.addFlashAttribute("successMessage", "Полная синхронизация умного дома завершена.");
        return "redirect:/dashboard";
    }
}
