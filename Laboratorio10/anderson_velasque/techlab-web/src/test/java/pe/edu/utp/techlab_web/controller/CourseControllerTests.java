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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.exception.InvalidCourseDataException;
import pe.edu.utp.techlab_web.service.CourseService;

@WebMvcTest(CourseController.class)
class CourseControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CourseService service;

    @Test
    void postCreaRecursoYDevuelveLocation() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
                .willReturn(new CourseDTO(4L, "Java profesional", 24, "Backend"));
        mvc.perform(post("/api/v1/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"titulo\":\"Java profesional\",\"horas\":24,\"categoria\":\"Backend\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        endsWith("/api/v1/cursos/4")))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.titulo").value("Java profesional"));
    }

    @Test
    void putActualizaYDeleteElimina() throws Exception {
        given(service.actualizar(
                org.mockito.ArgumentMatchers.eq(4L),
                any(CourseInputDto.class)))
                .willReturn(new CourseDTO(4L, "Java web", 32, "Backend"));
        mvc.perform(put("/api/v1/cursos/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Java web\",\"horas\":32,\"categoria\":\"Backend\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horas").value(32));
        mvc.perform(delete("/api/v1/cursos/4"))
                .andExpect(status().isNoContent());
    }

    @Test
    void errorDeEntradaUsaProblemDetail() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
                .willThrow(new InvalidCourseDataException(
                        "El título es obligatorio"));
        mvc.perform(post("/api/v1/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"\",\"horas\":24,\"categoria\":\"Backend\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.detail").value(
                        "El título es obligatorio"));
    }
}
