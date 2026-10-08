package pe.edu.utp.techlab_web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pe.edu.utp.techlab_web.dto.CourseDTO;
import static org.mockito.BDDMockito.given;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.techlab_web.service.CourseService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CourseController.class, PortalController.class})
class WebRoutesTests {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CourseService service;

    @BeforeEach
    void prepararDatos() {
        var bootstrap = new CourseDTO(2L, "Bootstrap", 16, "Frontend");
        var spring = new CourseDTO(3L, "Spring Boot", 20, "Backend");
        given(service.buscar("")).willReturn(List.of(
                new CourseDTO(1L, "HTML y CSS", 12, "Frontend"), bootstrap, spring));
        given(service.buscar("SPRING")).willReturn(List.of(spring));
        given(service.buscarPorId(2L)).willReturn(Optional.of(bootstrap));
        given(service.buscarPorId(999L)).willReturn(Optional.empty());
        given(service.buscar("", "Backend")).willReturn(List.of(spring));
    }

    @Test
    void categoriaSeExponeYFiltraEnJson() throws Exception {
        mvc.perform(get("/api/v1/cursos").param("categoria", "Backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoria").value("Backend"));
        mvc.perform(get("/api/v1/cursos").param("categoria", "x".repeat(41)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listadoYFiltroFuncionan() throws Exception {
        mvc.perform(get("/api/v1/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/v1/cursos").param("q", "SPRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void detalleYErroresUsanEstadosHttp() throws Exception {
        mvc.perform(get("/api/v1/cursos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Bootstrap"));
        mvc.perform(get("/api/v1/cursos/999"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/cursos/abc"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/cursos").param("q", "x".repeat(61)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void metodoNoPermitidoYRedireccion() throws Exception {
        mvc.perform(post("/api/v1/cursos"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/portal"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cursos"));
    }
}
