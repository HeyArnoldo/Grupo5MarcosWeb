package pe.edu.utp.techlab_web.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pe.edu.utp.techlab_web.exception.CategoryNotFoundException;
import pe.edu.utp.techlab_web.exception.CourseNotFoundException;
import pe.edu.utp.techlab_web.exception.DuplicateCourseTitleException;
import pe.edu.utp.techlab_web.exception.InvalidCourseDataException;

@RestControllerAdvice(assignableTypes = CourseController.class)
public class CourseApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> invalidBody(
            MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()
                ));
        ProblemDetail body = createProblem(
                HttpStatus.BAD_REQUEST,
                "Datos inválidos",
                "Revise los campos indicados"
        );
        body.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InvalidCourseDataException.class)
    public ResponseEntity<ProblemDetail> invalidData(
            InvalidCourseDataException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Datos inválidos",
                exception.getMessage()
        );
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ProblemDetail> invalidCategory(
            CategoryNotFoundException exception) {
        ProblemDetail body = createProblem(
                HttpStatus.BAD_REQUEST,
                "Categoría inválida",
                exception.getMessage()
        );
        body.setProperty("errors", Map.of("categoriaId", exception.getMessage()));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(
            CourseNotFoundException exception) {
        return problem(
                HttpStatus.NOT_FOUND,
                "Curso no encontrado",
                exception.getMessage()
        );
    }

    @ExceptionHandler({
        DuplicateCourseTitleException.class,
        DataIntegrityViolationException.class
    })
    public ResponseEntity<ProblemDetail> conflict(
            RuntimeException exception) {
        String detail = exception instanceof DuplicateCourseTitleException
                ? exception.getMessage()
                : "La operación incumple una restricción de la base de datos";
        return problem(
                HttpStatus.CONFLICT,
                "Conflicto de datos",
                detail
        );
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String title,
            String detail) {
        return ResponseEntity
                .status(status)
                .body(createProblem(status, title, detail));
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        return body;
    }
}
