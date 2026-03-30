package ru.tbank.practicum.exceptionHandler;

import jakarta.persistence.EntityNotFoundException;
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

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }
}
