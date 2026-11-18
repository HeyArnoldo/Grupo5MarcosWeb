package pe.edu.utp.techlab_web.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import pe.edu.utp.techlab_web.dto.CourseDto;
import pe.edu.utp.techlab_web.service.CourseService;

@WebMvcTest({CourseController.class, PortalController.class})
class WebRoutesTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CourseService service;

    @BeforeEach
    void prepararDatos() {
        var bootstrap = new CourseDto(2L, "Bootstrap", 16, 1L, "Front-end");
        var spring = new CourseDto(3L, "Spring Boot", 20, 2L, "Back-end");
        given(service.buscar("SPRING")).willReturn(List.of(spring));
        given(service.buscarPorId(2L)).willReturn(java.util.Optional.of(bootstrap));
        given(service.buscarPorId(999L)).willReturn(java.util.Optional.empty());
    }

    @Test
    void categoriasExponenyFiltroEnJson() throws Exception {
        mvc.perform(get("/api/v1/cursos").param("q", "SPRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Spring Boot"));
    }
}
