package pe.edu.utp.Paway.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Database conflicts that a controller did not handle itself (a concurrent edit or a
 * constraint violation) are shown on the existing error page as 409 Conflict instead of 500.
 */
@ControllerAdvice
public class PersistenceErrorAdvice {
    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public String conflict(HttpServletResponse response, Model model) {
        response.setStatus(HttpStatus.CONFLICT.value());
        model.addAttribute("status", HttpStatus.CONFLICT.value());
        return "error/4xx";
    }
}
