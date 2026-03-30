package ru.tbank.practicum.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.tbank.practicum.entity.Radiator;
import ru.tbank.practicum.entity.RadiatorRule;
import ru.tbank.practicum.enums.EventSource;
import ru.tbank.practicum.service.RadiatorRuleService;
import ru.tbank.practicum.service.RadiatorService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/radiators")
public class RadiatorController {

    private final RadiatorService radiatorService;
    private final RadiatorRuleService radiatorRuleService;

    @GetMapping
    public String getRadiators(Model model) {
        model.addAttribute("radiators", radiatorService.getAll());
        return "radiators/index";
    }

    @GetMapping("/new")
    public String newRadiator(Model model) {
        model.addAttribute("radiator", new Radiator());
        return "radiators/new";
    }

    @PostMapping
    public String saveRadiator(@ModelAttribute("radiator") Radiator radiator) {
        radiatorService.save(radiator, EventSource.USER);
        return "redirect:/radiators";
    }

    @GetMapping("/{id}/edit")
    public String editRadiator(@PathVariable Long id, Model model) {
        model.addAttribute("radiator", radiatorService.getById(id));
        model.addAttribute("rules", radiatorRuleService.getByRadiatorId(id));
        model.addAttribute("newRule", new RadiatorRule());
        return "radiators/edit";
    }

    @PostMapping("/{radiatorId}/rules")
    public String addRule(@PathVariable Long radiatorId, @ModelAttribute("newRule") RadiatorRule rule) {
        rule.setRadiator(radiatorService.getById(radiatorId));
        radiatorRuleService.save(rule);
        return "redirect:/radiators/" + radiatorId + "/edit";
    }

    @PostMapping("/{radiatorId}/rules/{ruleId}/delete")
    public String deleteRule(@PathVariable Long radiatorId, @PathVariable Long ruleId) {
        radiatorRuleService.deleteById(ruleId, EventSource.USER);
        return "redirect:/radiators/" + radiatorId + "/edit";
    }

    @PostMapping("/{id}")
    public String updateRadiator(@PathVariable Long id, @ModelAttribute("radiator") Radiator radiator) {
        radiatorService.updateFromForm(id, radiator, EventSource.USER);
        return "redirect:/radiators";
    }

    @PostMapping("/{id}/delete")
    public String deleteRadiator(@PathVariable Long id) {
        radiatorService.deleteById(id);
        return "redirect:/radiators";
    }
}
