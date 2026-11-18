package pe.edu.utp.techlab_web.controller;

import static org.hamcrest.Matchers.endsWith;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import pe.edu.utp.techlab_web.dto.CourseDto;
import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.service.CourseService;

@WebMvcTest(CourseController.class)
class CourseControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CourseService service;

    @Test
    void postValidoCreaCursoRelacionado() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
                .willReturn(new CourseDto(
                        4L,
                        "Java profesional",
                        24,
                        2L,
                        "Back-end"
                ));

        mvc.perform(post("/api/v1/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                    "titulo": "Java profesional",
                                    "horas": 24,
                                    "categoriaId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        endsWith("/api/v1/cursos/4")
                ))
                .andExpect(jsonPath("$.categoriaId").value(2))
                .andExpect(jsonPath("$.categoriaNombre").value("Back-end"));
    }

    @Test
    void postInvalidoDevuelveErroresPorCampo() throws Exception {
        mvc.perform(post("/api/v1/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                    "titulo": "",
                                    "horas": 0,
                                    "categoriaId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.errors.titulo").exists())
                .andExpect(jsonPath("$.errors.horas").exists())
                .andExpect(jsonPath("$.errors.categoriaId").exists());
    }
}
