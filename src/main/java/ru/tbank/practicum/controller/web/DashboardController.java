package ru.tbank.practicum.controller.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.tbank.practicum.dto.CurtainsSchedule;
import ru.tbank.practicum.model.BlindsState;
import ru.tbank.practicum.service.SmartHomeService;

@Controller
public class DashboardController {

    private final SmartHomeService smartHomeService;

    public DashboardController(SmartHomeService smartHomeService) {
        this.smartHomeService = smartHomeService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("temp", smartHomeService.getRadiatorTargetTemp());
        model.addAttribute("blinds", smartHomeService.getBlindsState());
        model.addAttribute("schedule", smartHomeService.getSchedule());
        return "dashboard";
    }

    @PostMapping("/radiator")
    public String setRadiator(@RequestParam @Min(10) @Max(35) int temp) {
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
        smartHomeService.setSchedule(new CurtainsSchedule(LocalTime.parse(openAt), LocalTime.parse(closeAt), enabled));
        return "redirect:/";
    }
}
