package pe.edu.utp.techlab_web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.exception.DuplicateCourseTitleException;
import pe.edu.utp.techlab_web.service.CourseService;

@WebMvcTest({ CourseViewController.class, PortalController.class })
class CourseViewControllerTests {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CourseService service;
    private CourseDTO bootstrap;

    @BeforeEach
    void prepararDatos() {
        bootstrap = new CourseDTO(2L, "Bootstrap", 16);
        given(service.buscarPorId(2L))
                .willReturn(Optional.of(bootstrap));
    }

    @Test
    void formularioNuevoTieneObjetoDeEntrada() throws Exception {
        mvc.perform(get("/cursos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/formulario"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("tituloPagina", "Nuevo curso"))
                .andExpect(content().string(containsString("Registrar curso")));
    }

    @Test
    void crearValidoAplicaPostRedirectGet() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
                .willReturn(new CourseDTO(4L, "Java profesional", 24));
        mvc.perform(post("/cursos")
                .param("titulo", "Java profesional")
                .param("horas", "24"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos/4"))
                .andExpect(flash().attribute(
                        "mensaje",
                        "Curso registrado correctamente"));
    }

    @Test
    void duplicadoRegresaAlFormularioConLosDatos() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
                .willThrow(new DuplicateCourseTitleException("Bootstrap"));
        mvc.perform(post("/cursos")
                .param("titulo", "Bootstrap")
                .param("horas", "18"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/formulario"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute(
                        "error",
                        "Ya existe un curso con el título Bootstrap"));
    }

    @Test
    void editarCargaDatosYEliminarRedirige() throws Exception {
        mvc.perform(get("/cursos/2/editar"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cursoId", 2L))
                .andExpect(model().attribute("tituloPagina", "Editar curso"));
        willDoNothing().given(service).eliminar(2L);
        mvc.perform(post("/cursos/2/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos"))
                .andExpect(flash().attribute(
                        "mensaje",
                        "Curso eliminado correctamente"));
    }
}