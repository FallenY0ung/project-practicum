package ru.tbank.practicum.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.tbank.practicum.entity.Blinds;
import ru.tbank.practicum.entity.Schedule;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.service.BlindsService;
import ru.tbank.practicum.service.ScheduleService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/blinds")
public class BlindsController {

    private final BlindsService blindsService;
    private final ScheduleService scheduleService;

    @GetMapping
    public String getBlinds(Model model) {
        model.addAttribute("blindsList", blindsService.getAll());
        return "blinds/index";
    }

    @GetMapping("/new")
    public String newBlinds(Model model) {
        model.addAttribute("blinds", new Blinds());
        return "blinds/new";
    }

    @PostMapping
    public String saveBlinds(@ModelAttribute("blinds") Blinds blinds) {
        blindsService.save(blinds);
        return "redirect:/blinds";
    }

    @GetMapping("/{id}/edit")
    public String editBlinds(@PathVariable Long id, Model model) {
        model.addAttribute("blinds", blindsService.getById(id));
        model.addAttribute("schedules", scheduleService.getByBlindsId(id));
        model.addAttribute("newSchedule", new Schedule());
        return "blinds/edit";
    }

    @PostMapping("/{blindsId}/schedules")
    public String addRule(@PathVariable Long blindsId, @ModelAttribute("newSchedule") Schedule schedule) {
        schedule.setBlinds(blindsService.getById(blindsId));
        scheduleService.save(schedule);
        return "redirect:/blinds/" + blindsId + "/edit";
    }

    @PostMapping("/{blindId}/schedules/{scheduleId}/delete")
    public String deleteRule(@PathVariable Long blindId, @PathVariable Long scheduleId) {
        scheduleService.deleteById(scheduleId, EventSource.USER);
        return "redirect:/blinds/" + blindId + "/edit";
    }

    @PostMapping("/{id}")
    public String updateBlinds(@PathVariable Long id,
                               @ModelAttribute("blinds") Blinds blinds) {
        blindsService.updateFromForm(id, blinds, EventSource.USER);
        return "redirect:/blinds";
    }

    @PostMapping("/{id}/delete")
    public String deleteBlinds(@PathVariable Long id) {
        blindsService.deleteById(id);
        return "redirect:/blinds";
    }
}