package pe.edu.utp.techlab_web.persistence;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.repository.CourseRepository;
import pe.edu.utp.techlab_web.service.CourseService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CoursePersistenceIntegrationTests {
    @Autowired
    private CourseService service;

    @Autowired
    private CourseRepository repository;

    @Test
    void flywayCargaCatalogoYCategorias() {
        assertEquals(3L, service.cantidad());
        assertEquals(48, service.totalHoras());
        assertEquals(List.of("HTML y CSS", "Bootstrap", "Spring Boot"),
                service.buscar("").stream().map(CourseDTO::titulo).toList());
        assertEquals(List.of("Frontend", "Frontend", "Backend"),
                service.buscar("").stream().map(CourseDTO::categoria).toList());
    }

    @Test
    void repositorioBuscaSinDistinguirMayusculas() {
        var resultado = repository.findByTituloContainingIgnoreCaseOrderByIdAsc("bOoT");
        assertEquals(1, resultado.size());
        assertEquals("Spring Boot", resultado.getFirst().getTitulo());
    }

    @Test
    void categoriaSeConsultaEnMysql() {
        var resultado = repository.findByCategoriaIgnoreCaseOrderByIdAsc("frontend");
        assertEquals(List.of(1L, 2L), resultado.stream().map(c -> c.getId()).toList());
        assertEquals(List.of("Spring Boot"), service.buscar(" boot ", " backend ")
                .stream().map(CourseDTO::titulo).toList());
        assertTrue(service.buscar("", "Inexistente").isEmpty());
    }

    @Test
    void detalleDevuelveDtoOAusencia() {
        assertEquals(new CourseDTO(2L, "Bootstrap", 16, "Frontend"),
                service.buscarPorId(2L).orElseThrow());
        assertTrue(service.buscarPorId(999L).isEmpty());
    }
}
