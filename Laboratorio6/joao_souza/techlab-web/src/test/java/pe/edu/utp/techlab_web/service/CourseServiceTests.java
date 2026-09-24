package pe.edu.utp.techlab_web.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CourseServiceTests {
    private final CourseService service = new CourseService();
    @Test
    void consultaVaciaDevuelveTodos() {
        assertEquals(3, service.buscar("").size());
    }
    @Test
    void busquedaNormalizaEspaciosYMayusculas() {
        var resultado = service.buscar(" SPRING ");
        assertEquals(1, resultado.size());
        assertEquals(3L, resultado.getFirst().id());
    }
    @Test
    void idDesconocidoNoExiste() {
        assertTrue(service.buscarPorId(999L).isEmpty());
    }
}