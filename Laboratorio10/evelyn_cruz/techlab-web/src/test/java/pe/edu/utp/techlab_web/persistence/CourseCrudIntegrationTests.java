package pe.edu.utp.techlab_web.persistence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.exception.DuplicateCourseTitleException;
import pe.edu.utp.techlab_web.exception.InvalidCourseDataException;
import pe.edu.utp.techlab_web.service.CourseService;
@SpringBootTest
@Transactional
class CourseCrudIntegrationTests {
 @Autowired
 private CourseService service;
 @Test
 void cicloCrudCompletoSeEjecutaContraMySql() {
 String titulo = "Prueba CRUD " + System.nanoTime();
 var creado = service.crear(
 new CourseInputDto(titulo, 24)
 );
 assertNotNull(creado.id());
 assertEquals(24, creado.horas());
 var actualizado = service.actualizar(
 creado.id(),
 new CourseInputDto(titulo + " actualizado", 32)
 );
 assertEquals(32, actualizado.horas());
 assertEquals(
 titulo + " actualizado",
 service.buscarPorId(creado.id()).orElseThrow().titulo()
 );
 service.eliminar(creado.id());
 assertFalse(service.buscarPorId(creado.id()).isPresent());
 }
 @Test
 void reglasDeNegocioRechazanDatosInvalidosYDuplicados() {
 assertThrows(
 InvalidCourseDataException.class,
 () -> service.crear(new CourseInputDto(" ", 0))
 );
 assertThrows(
 InvalidCourseDataException.class,
 () -> service.buscar("x".repeat(61))
 );
 assertThrows(
 DuplicateCourseTitleException.class,
 () -> service.crear(
 new CourseInputDto(" spring boot ", 20)
 )
 );
 }
}
