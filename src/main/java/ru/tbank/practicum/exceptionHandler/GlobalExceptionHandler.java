package ru.tbank.practicum.exceptionHandler;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.tbank.practicum.exception.CurtainsScheduleException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CurtainsScheduleException.class)
    public String curtainsScheduleException(CurtainsScheduleException e, Model model) {
        model.addAttribute("errormessage", e.getMessage());
        return "curtainsError";
    }
}
