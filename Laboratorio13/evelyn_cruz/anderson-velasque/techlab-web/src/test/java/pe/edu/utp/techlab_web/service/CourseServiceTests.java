package pe.edu.utp.techlab_web.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import pe.edu.utp.techlab_web.dto.CourseDto;
import pe.edu.utp.techlab_web.entity.Course;
import pe.edu.utp.techlab_web.mapper.CourseMapper;
import pe.edu.utp.techlab_web.repository.CategoryRepository;
import pe.edu.utp.techlab_web.repository.CourseRepository;

class CourseServiceTests {

    private CourseRepository repository;
    private CategoryRepository categoryRepository;
    private CourseMapper mapper;
    private CourseService service;

    @BeforeEach
    void prepararServicio() {
        repository = mock(CourseRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        mapper = mock(CourseMapper.class);
        service = new CourseService(repository, categoryRepository, mapper);
    }

    @Test
    void consultaVaciaONulaConsultaTodos() {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of());
        assertTrue(service.buscar(null).isEmpty());
        assertTrue(service.buscar(" ").isEmpty());
        verify(repository, times(2)).findAllByOrderByIdAsc();
    }

    @Test
    void busquedaNormalizaEspaciosYRetornaDto() {
        var course = mock(Course.class);
        var dto = new CourseDto(3L, "Spring Boot", 20, 2L, "Back-end");
        when(repository.findByTituloContainingIgnoreCaseOrderByIdAsc("SPRING"))
                .thenReturn(List.of(course));
        when(mapper.toDto(course)).thenReturn(dto);
        assertEquals(List.of(dto), service.buscar(" SPRING "));
    }

    @Test
    void idDesconocidoNoExiste() {
        when(repository.findDetailById(999L)).thenReturn(Optional.empty());
        assertTrue(service.buscarPorId(999L).isEmpty());
    }
}
