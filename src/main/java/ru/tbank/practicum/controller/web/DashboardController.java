package ru.tbank.practicum.controller.web;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.tbank.practicum.dto.CurtainsSchedule;
import ru.tbank.practicum.exception.CurtainsScheduleException;
import ru.tbank.practicum.enums.BlindsState;
import ru.tbank.practicum.service.SmartHomeService;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    // для фейк коммита
    private final SmartHomeService smartHomeService;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("temp", smartHomeService.getRadiatorTargetTemp());
        model.addAttribute("blinds", smartHomeService.getBlindsState());
        model.addAttribute("schedule", smartHomeService.getSchedule());
        return "dashboard";
    }

    @PostMapping("/radiator")
    public String setRadiator(@RequestParam AtomicInteger temp) {
        smartHomeService.setRadiatorTargetTemp(temp);
        return "redirect:/";
    }

    @PostMapping("/blinds")
    public String setBlinds(@RequestParam BlindsState state) {
        smartHomeService.setBlindsState(state);
        return "redirect:/";
    }

    @PostMapping("/curtains/schedule")
    public String setSchedule(
            @RequestParam String openAt,
            @RequestParam String closeAt,
            @RequestParam(defaultValue = "false") boolean enabled) {
        try {
            smartHomeService.setSchedule(
                    new CurtainsSchedule(LocalTime.parse(openAt), LocalTime.parse(closeAt), enabled));
            return "redirect:/";
        } catch (DateTimeParseException e) {
            throw new CurtainsScheduleException("Некорректный формат времени. Используй HH:mm");
        }
    }
}
