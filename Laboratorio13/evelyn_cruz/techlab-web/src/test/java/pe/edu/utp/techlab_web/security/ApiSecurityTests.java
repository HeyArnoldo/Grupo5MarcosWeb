package pe.edu.utp.techlab_web.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import pe.edu.utp.techlab_web.config.SecurityConfig;
import pe.edu.utp.techlab_web.controller.CourseController;
import pe.edu.utp.techlab_web.service.CourseService;

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.demo-username=docente",
        "app.security.demo-password=Laboratorio-S13-Temporal!"
})
class ApiSecurityTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CourseService service;

    @Test
    void apiSinCredencialesResponde401() throws Exception {
        mvc.perform(get("/api/v1/cursos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiConCredencialesCorrectasResponde200() throws Exception {
        mvc.perform(get("/api/v1/cursos")
                .with(httpBasic("docente", "Laboratorio-S13-Temporal!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void apiConClaveIncorrectaResponde401() throws Exception {
        mvc.perform(get("/api/v1/cursos")
                .with(httpBasic("docente", "incorrecta")))
                .andExpect(status().isUnauthorized());
    }
}
