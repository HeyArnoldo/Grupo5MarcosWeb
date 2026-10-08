package pe.edu.utp.techlab_web.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.service.CourseService;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CourseViewController.class, PortalController.class})
class CourseViewControllerTests {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CourseService service;

    private final CourseDTO bootstrap = new CourseDTO(2L, "Bootstrap", 16, "Frontend");
    private final CourseDTO spring = new CourseDTO(3L, "Spring Boot", 20, "Backend");

    @BeforeEach
    void prepararDatos() {
        given(service.buscar("")).willReturn(List.of(
                new CourseDTO(1L, "HTML y CSS", 12, "Frontend"), bootstrap, spring));
    }

    @Test
    void listadoRenderizaModeloYHtml() throws Exception {
        mvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/lista"))
                .andExpect(model().attribute("q", ""))
                .andExpect(model().attribute("cursos", hasSize(3)))
                .andExpect(content().string(containsString("Catálogo de cursos")))
                .andExpect(content().string(containsString("Frontend")));
    }

    @Test
    void filtroConservaConsultaYReduceResultados() throws Exception {
        given(service.buscar("Spring")).willReturn(List.of(spring));
        mvc.perform(get("/cursos").param("q", " Spring "))
                .andExpect(status().isOk())
                .andExpect(model().attribute("q", "Spring"))
                .andExpect(model().attribute("cursos", hasSize(1)))
                .andExpect(content().string(containsString("Spring Boot")));
    }

    @Test
    void categoriaFiltraSinCambiarRuta() throws Exception {
        given(service.buscar("", "Backend")).willReturn(List.of(spring));
        mvc.perform(get("/cursos").param("categoria", " Backend "))
                .andExpect(status().isOk())
                .andExpect(model().attribute("categoria", "Backend"))
                .andExpect(model().attribute("cursos", hasSize(1)));
    }

    @Test
    void detalleMuestraCursoYCategoria() throws Exception {
        given(service.buscarPorId(2L)).willReturn(Optional.of(bootstrap));
        mvc.perform(get("/cursos/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/detalle"))
                .andExpect(model().attribute("curso", bootstrap))
                .andExpect(content().string(containsString("Bootstrap")))
                .andExpect(content().string(containsString("Frontend")));
    }

    @Test
    void resumenMuestraCantidadYHoras() throws Exception {
        given(service.cantidad()).willReturn(3L);
        given(service.totalHoras()).willReturn(48);
        mvc.perform(get("/cursos/resumen"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/resumen"))
                .andExpect(model().attribute("cantidadCursos", 3L))
                .andExpect(model().attribute("totalHoras", 48));
    }

    @Test
    void erroresConservanEstadosHttp() throws Exception {
        given(service.buscarPorId(999L)).willReturn(Optional.empty());
        mvc.perform(get("/cursos/999")).andExpect(status().isNotFound());
        mvc.perform(get("/cursos/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/cursos").param("q", "x".repeat(61)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/cursos").param("categoria", "x".repeat(41)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void portalRedirigeAlCatalogo() throws Exception {
        mvc.perform(get("/portal"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cursos"));
    }
}
