package pe.edu.utp.techlab_web.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pe.edu.utp.techlab_web.config.SecurityConfig;
import pe.edu.utp.techlab_web.controller.CourseViewController;
import pe.edu.utp.techlab_web.controller.LoginController;
import pe.edu.utp.techlab_web.dto.CategoryDto;
import pe.edu.utp.techlab_web.dto.CourseDto;
import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.service.CourseService;
import pe.edu.utp.techlab_web.web.GlobalViewModel;

@WebMvcTest({
    CourseViewController.class,
    LoginController.class,
    GlobalViewModel.class
})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "app.security.demo-username=docente",
    "app.security.demo-password=Laboratorio-S13-Temporal!"
})
class WebSecurityTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CourseService service;

    @BeforeEach
    void prepararDatos() {
        given(service.listarCategorias()).willReturn(List.of(
                new CategoryDto(2L, "Back-end")
        ));
    }

    @Test
    void catalogoEsPublico() throws Exception {
        mvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/lista"));
    }

    @Test
    void formularioProtegidoRedirigeAlLogin() throws Exception {
        mvc.perform(get("/cursos/nuevo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void usuarioAutenticadoAbreFormulario() throws Exception {
        mvc.perform(get("/cursos/nuevo").with(user("docente")))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/formulario"));
    }

    @Test
    void postSinCsrfEsRechazado() throws Exception {
        mvc.perform(post("/cursos")
                .with(user("docente"))
                .param("titulo", "Seguridad web")
                .param("horas", "24")
                .param("categoriaId", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postAutenticadoConCsrfContinuaAlControlador() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
                .willReturn(new CourseDto(8L, "Seguridad web", 24, 2L, "Back-end"));

        mvc.perform(post("/cursos")
                .with(user("docente"))
                .with(csrf())
                .param("titulo", "Seguridad web")
                .param("horas", "24")
                .param("categoriaId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos/8"));
    }
}
