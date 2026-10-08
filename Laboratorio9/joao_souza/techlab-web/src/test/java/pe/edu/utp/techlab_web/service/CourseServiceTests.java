package pe.edu.utp.techlab_web.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.entity.Course;
import pe.edu.utp.techlab_web.mapper.CourseMapper;
import pe.edu.utp.techlab_web.repository.CourseRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseServiceTests {
    private CourseRepository repository;
    private CourseMapper mapper;
    private CourseService service;

    @BeforeEach
    void prepararServicio() {
        repository = mock(CourseRepository.class);
        mapper = mock(CourseMapper.class);
        service = new CourseService(repository, mapper);
    }

    @Test
    void consultaVaciaONulaConsultaTodos() {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of());
        assertTrue(service.buscar(null).isEmpty());
        assertTrue(service.buscar("  ").isEmpty());
        verify(repository, times(2)).findAllByOrderByIdAsc();
    }

    @Test
    void busquedaNormalizaEspaciosYRetornaDto() {
        var course = mock(Course.class);
        var dto = new CourseDTO(3L, "Spring Boot", 20, "Backend");
        when(repository.findByTituloContainingIgnoreCaseOrderByIdAsc("SPRING"))
                .thenReturn(List.of(course));
        when(mapper.toDto(course)).thenReturn(dto);
        assertEquals(List.of(dto), service.buscar(" SPRING "));
    }

    @Test
    void categoriaYTituloSeConsultanEnRepositorio() {
        when(repository.findByCategoriaIgnoreCaseOrderByIdAsc("Frontend"))
                .thenReturn(List.of());
        when(repository.findByTituloContainingIgnoreCaseAndCategoriaIgnoreCaseOrderByIdAsc(
                "Boot", "Backend")).thenReturn(List.of());
        service.buscar("", " Frontend ");
        service.buscar(" Boot ", " Backend ");
        verify(repository).findByCategoriaIgnoreCaseOrderByIdAsc("Frontend");
        verify(repository).findByTituloContainingIgnoreCaseAndCategoriaIgnoreCaseOrderByIdAsc(
                "Boot", "Backend");
    }

    @Test
    void idDesconocidoNoExiste() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(service.buscarPorId(999L).isEmpty());
        verifyNoInteractions(mapper);
    }

    @Test
    void resumenConsultaAgregadosYAdmiteCatalogoVacio() {
        when(repository.count()).thenReturn(3L);
        when(repository.sumarHoras()).thenReturn(48L, null);
        assertEquals(3L, service.cantidad());
        assertEquals(48, service.totalHoras());
        assertEquals(0, service.totalHoras());
    }

    @Test
    void sumaFueraDeRangoNoSeTrunca() {
        when(repository.sumarHoras()).thenReturn((long) Integer.MAX_VALUE + 1);
        assertThrows(ArithmeticException.class, service::totalHoras);
    }
}
