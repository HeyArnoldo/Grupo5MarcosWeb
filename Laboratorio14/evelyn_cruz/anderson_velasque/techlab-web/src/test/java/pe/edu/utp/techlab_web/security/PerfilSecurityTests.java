package pe.edu.utp.techlab_web.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import pe.edu.utp.techlab_web.config.SecurityConfig;
import pe.edu.utp.techlab_web.controller.PerfilController;
import pe.edu.utp.techlab_web.web.GlobalViewModel;

@WebMvcTest({ PerfilController.class, GlobalViewModel.class })
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.demo-username=docente",
        "app.security.demo-password=Laboratorio-S13-Temporal!"
})
class PerfilSecurityTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void anonimoEsRedirigidoAlLogin() throws Exception {
        mvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void autenticadoVeLaVistaPerfil() throws Exception {
        mvc.perform(get("/perfil").with(user("docente")))
                .andExpect(status().isOk())
                .andExpect(view().name("perfil"));
    }
}