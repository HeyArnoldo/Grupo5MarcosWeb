package pe.edu.utp.Paway.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pe.edu.utp.Paway.service.ComplaintService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LegalFlowTests {
    private static final Path STORAGE;
    static {
        try { STORAGE = Files.createTempDirectory("paway-complaints-test-"); }
        catch (java.io.IOException ex) { throw new ExceptionInInitializerError(ex); }
    }
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("paway.complaints.directory", () -> STORAGE.toString());
    }
    @Autowired MockMvc mvc;
    @Autowired ComplaintService complaints;

    private MockHttpServletRequestBuilder submission(MockHttpSession session) {
        return post("/libro-de-reclamaciones").session(session)
                .param("submissionToken", (String) session.getAttribute("complaintToken"))
                .param("name", "Consumidor de prueba").param("document", "12345678")
                .param("address", "Lima, Perú").param("email", "consumer@example.com")
                .param("phone", "987654321").param("description", "Servicio de envío")
                .param("detail", "El paquete llegó con daños.").param("request", "Solicito evaluación.");
    }

    @Test
    void exposesLegalPagesAndRegistersDurableIdempotentSheetAndResponse() throws Exception {
        mvc.perform(get("/terminos-y-condiciones")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Ley N.º 29571")));
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/libro-de-reclamaciones").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("/images/libro-reclamaciones.svg")));
        String token = (String) session.getAttribute("complaintToken");
        String receipt = "/libro-de-reclamaciones/constancia/" + token;
        int count = complaints.all().size();
        mvc.perform(submission(session)).andExpect(redirectedUrl(receipt));
        mvc.perform(submission(session)).andExpect(redirectedUrl(receipt));
        assertThat(complaints.all()).hasSize(count + 1);
        ComplaintService reopened = new ComplaintService(STORAGE.toString());
        assertThat(reopened.get(token).details().get("detail")).isEqualTo("El paquete llegó con daños.");
        mvc.perform(get(receipt)).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().string(containsString("Imprimir / guardar PDF")))
                .andExpect(content().string(containsString("Pendiente de atención")));
        mvc.perform(get("/admin/reclamaciones")).andExpect(redirectedUrl("/login"));
        MockHttpSession admin = new MockHttpSession();
        mvc.perform(post("/login").session(admin).param("profile", "admin"));
        mvc.perform(get("/admin/reclamaciones").session(admin)).andExpect(status().isOk());
        mvc.perform(post("/admin/reclamaciones/" + token + "/respuesta").session(admin)
                .param("adminToken", (String) admin.getAttribute("complaintAdminToken"))
                .param("answer", "Se realizará la devolución.").param("delivery", "Correo enviado, referencia 123"))
                .andExpect(redirectedUrl("/admin/reclamaciones"));
        mvc.perform(get(receipt)).andExpect(content().string(containsString("Se realizará la devolución.")));
        assertThat(reopened.get(token).delivery()).contains("referencia 123");
    }

    @Test
    void rejectsInvalidFormsMissingGuardianAndForgedSubmission() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/libro-de-reclamaciones").session(session));
        int count = complaints.all().size();
        mvc.perform(submission(session).param("email", "invalid").param("type", "OTHER"))
                .andExpect(model().attributeHasFieldErrors("complaintForm", "email", "type"));
        mvc.perform(submission(session).param("minor", "true"))
                .andExpect(model().attributeHasFieldErrors("complaintForm", "guardianComplete"));
        mvc.perform(submission(session).param("amount", "-20"))
                .andExpect(model().attributeHasFieldErrors("complaintForm", "amount"));
        mvc.perform(submission(session).with(request -> { request.setParameter("submissionToken", "forged"); return request; }))
                .andExpect(status().isForbidden());
        assertThat(complaints.all()).hasSize(count);
        mvc.perform(get("/libro-de-reclamaciones/constancia/not-a-token")).andExpect(status().isNotFound());
    }
}
