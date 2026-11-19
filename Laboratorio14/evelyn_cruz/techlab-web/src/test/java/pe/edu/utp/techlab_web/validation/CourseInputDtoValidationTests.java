package pe.edu.utp.techlab_web.validation;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import pe.edu.utp.techlab_web.dto.CourseInputDto;

class CourseInputDtoValidationTests {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    @Test
    void entradaValidaNoProduceViolaciones() {
        var input = new CourseInputDto(
                "Spring Data JPA",
                24,
                2L
        );
        assertTrue(validator.validate(input).isEmpty());
    }

    @Test
    void entradaInvalidaIdentificaCadaCampo() {
        var input = new CourseInputDto(" ", 0, null);
        Set<ConstraintViolation<CourseInputDto>> violations = validator.validate(input);
        Set<String> campos = violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
        assertEquals(
                Set.of("titulo", "horas", "categoriaId"),
                campos
        );
    }
}
