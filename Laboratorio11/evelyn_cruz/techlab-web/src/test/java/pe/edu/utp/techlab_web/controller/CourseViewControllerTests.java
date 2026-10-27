package pe.edu.utp.techlab_web.controller;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import pe.edu.utp.techlab_web.dto.CategoryDto;
import pe.edu.utp.techlab_web.dto.CourseDto;
import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.service.CourseService;

@WebMvcTest({CourseViewController.class})
class CourseViewControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CourseService service;

    @BeforeEach
    void prepararCategorias() {
        given(service.listarCategorias()).willReturn(List.of(
                new CategoryDto(2L, "Back-end"),
                new CategoryDto(1L, "Front-end")
        ));
    }

    @Test
    void formularioNuevoCargaCategorias() throws Exception {
        mvc.perform(get("/cursos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/formulario"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("categorias", hasSize(2)));
    }

    @Test
    void formularioInvalidoMuestraErroresPorCampo() throws Exception {
        mvc.perform(post("/cursos")
                .param("titulo", "")
                .param("horas", "0")
                .param("categoriaId", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/formulario"))
                .andExpect(model().attributeHasFieldErrors(
                        "form",
                        "titulo",
                        "horas",
                        "categoriaId"
                ))
                .andExpect(model().attribute("categorias", hasSize(2)));
    }

    @Test
    void formularioValidoAplicaPostRedirectGet() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
                .willReturn(new CourseDto(
                        4L,
                        "Java profesional",
                        24,
                        2L,
                        "Back-end"
                ));

        mvc.perform(post("/cursos")
                .param("titulo", "Java profesional")
                .param("horas", "24")
                .param("categoriaId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos/4"))
                .andExpect(flash().attribute(
                        "mensaje",
                        "Curso registrado correctamente"
                ));
    }
}
