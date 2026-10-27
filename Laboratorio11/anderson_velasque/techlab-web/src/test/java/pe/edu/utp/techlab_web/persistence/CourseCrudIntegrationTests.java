package pe.edu.utp.techlab_web.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.exception.CategoryNotFoundException;
import pe.edu.utp.techlab_web.service.CourseService;

@SpringBootTest
@Transactional
class CourseCrudIntegrationTests {

    @Autowired
    private CourseService service;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void cursoPersisteConSuCategoria() {
        long categoriaId = service.listarCategorias()
                .stream()
                .filter(category -> category.nombre().equals("Back-end"))
                .findFirst()
                .orElseThrow()
                .id();
        String titulo = "Relaciones JPA " + System.nanoTime();
        var creado = service.crear(
                new CourseInputDto(titulo, 24, categoriaId)
        );
        assertNotNull(creado.id());
        assertEquals(categoriaId, creado.categoriaId());
        assertEquals("Back-end", creado.categoriaNombre());

        Long foreignKey = jdbc.queryForObject(
                "SELECT categoria_id FROM cursos WHERE id = ?",
                Long.class,
                creado.id()
        );
        assertEquals(categoriaId, foreignKey);
    }

    @Test
    void servicioRechazaCategoriaInexistente() {
        var input = new CourseInputDto(
                "Curso sin categoría real",
                12,
                999999L
        );
        assertThrows(
                CategoryNotFoundException.class,
                () -> service.crear(input)
        );
    }

    @Test
    void foreignKeyImpideReferenciaInexistente() {
        String titulo = "FK inválida " + System.nanoTime();
        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbc.update(
                        "INSERT INTO cursos (titulo, horas, categoria_id) VALUES (?, ?, ?)",
                        titulo,
                        12,
                        999999L
                )
        );
    }
}
