package pe.edu.utp.Paway.controller;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import pe.edu.utp.Paway.config.AccountSession;
import pe.edu.utp.Paway.dto.RegisterForm;
import pe.edu.utp.Paway.model.Shipment;
import pe.edu.utp.Paway.service.ShipmentService;
import pe.edu.utp.Paway.support.PostgresTestConfiguration;
import pe.edu.utp.Paway.support.TestAccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@AutoConfigureMockMvc
class AccountFlowTests {
    private static final String LOGIN_ERROR = "Correo o contraseña incorrectos.";
    @Autowired private MockMvc mvc;
    @Autowired private ShipmentService shipments;
    @Autowired private JdbcTemplate jdbc;

    static MockHttpServletRequestBuilder registration(MockHttpSession session, RegisterForm form) {
        return post("/registro").session(session).param("fullName", form.getFullName())
                .param("document", form.getDocument()).param("email", form.getEmail())
                .param("phone", form.getPhone()).param("password", form.getPassword())
                .param("passwordConfirmation", form.getPasswordConfirmation());
    }

    private MockHttpServletRequestBuilder login(MockHttpSession session, String email, String password) {
        return post("/login").session(session).param("email", email).param("password", password);
    }

    private int accounts() {
        return jdbc.queryForObject("select count(*) from account", Integer.class);
    }

    @Test
    void rendersLoginAndRegisterPagesWithoutDemoProfiles() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("autocomplete=\"current-password\"")))
                .andExpect(content().string(containsString("Crear cuenta")))
                .andExpect(content().string(not(containsString("name=\"profile\""))))
                .andExpect(content().string(not(containsString("Acceso de demostración"))));
        mvc.perform(get("/registro")).andExpect(status().isOk())
                .andExpect(content().string(containsString("autocomplete=\"new-password\"")))
                .andExpect(content().string(containsString("autocomplete=\"tel\"")))
                .andExpect(content().string(containsString("autocomplete=\"name\"")));
    }

    @Test
    void signUpLogsInTheNewCustomerAndLoginWorksAfterLogout() throws Exception {
        RegisterForm form = TestAccounts.form();
        MockHttpSession session = new MockHttpSession();
        mvc.perform(registration(session, form)).andExpect(redirectedUrl("/cliente"));
        mvc.perform(get("/cliente").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString(form.getFullName())));
        assertThat(session.getAttribute(AccountSession.ACCOUNT_ID)).isInstanceOf(Long.class);
        assertThat(session.getAttribute(AccountSession.ROLE)).isEqualTo("CLIENT");

        mvc.perform(post("/logout").session(session)).andExpect(redirectedUrl("/"));
        MockHttpSession again = new MockHttpSession();
        mvc.perform(login(again, form.getEmail().toUpperCase(), form.getPassword())).andExpect(redirectedUrl("/cliente"));
        mvc.perform(get("/cliente").session(again)).andExpect(status().isOk());
    }

    @Test
    void wrongPasswordAndUnknownEmailShowTheSameMessageAndDoNotSignIn() throws Exception {
        RegisterForm form = TestAccounts.form();
        mvc.perform(registration(new MockHttpSession(), form));

        MockHttpSession wrongPassword = new MockHttpSession();
        MvcResult wrong = mvc.perform(login(wrongPassword, form.getEmail(), "otra-clave-123"))
                .andExpect(status().isOk()).andExpect(model().attribute("loginError", LOGIN_ERROR)).andReturn();
        MockHttpSession unknownEmail = new MockHttpSession();
        MvcResult unknown = mvc.perform(login(unknownEmail, "nadie-" + form.getEmail(), "otra-clave-123"))
                .andExpect(status().isOk()).andExpect(model().attribute("loginError", LOGIN_ERROR)).andReturn();
        String wrongBody = wrong.getResponse().getContentAsString().replace(form.getEmail(), "EMAIL");
        String unknownBody = unknown.getResponse().getContentAsString().replace("nadie-" + form.getEmail(), "EMAIL");
        assertThat(wrongBody).isEqualTo(unknownBody).doesNotContain("otra-clave-123");

        assertThat(wrongPassword.getAttribute(AccountSession.ACCOUNT_ID)).isNull();
        assertThat(unknownEmail.getAttribute(AccountSession.ACCOUNT_ID)).isNull();
        mvc.perform(get("/cliente").session(wrongPassword)).andExpect(redirectedUrl("/login"));
        // Demo customers from V2 have no account, so nobody can sign in as them.
        mvc.perform(login(new MockHttpSession(), "brayan@example.com", "cualquier-clave"))
                .andExpect(model().attribute("loginError", LOGIN_ERROR));
    }

    @Test
    void loginChangesTheSessionIdAndKeepsThePendingQuote() throws Exception {
        RegisterForm form = TestAccounts.form();
        mvc.perform(registration(new MockHttpSession(), form));
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/cotizar").session(session).param("province", "lima").param("district", "Lince")
                .param("category", "textil").param("weight", "1").param("quantity", "1")
                .param("requestShipment", "true")).andExpect(redirectedUrl("/login"));
        String before = session.getId();

        mvc.perform(login(session, form.getEmail(), form.getPassword())).andExpect(redirectedUrl("/cliente/envios/nuevo"));

        assertThat(session.getId()).isNotEqualTo(before);
        assertThat(session.getAttribute("pendingQuote")).isNotNull();
        mvc.perform(get("/cliente/envios/nuevo").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Lince\" selected=\"selected\"")));
    }

    @Test
    void signUpWithAPendingQuoteLandsOnTheShipmentForm() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/cotizar").session(session).param("province", "lima").param("district", "Lince")
                .param("category", "textil").param("weight", "1").param("quantity", "1")
                .param("requestShipment", "true")).andExpect(redirectedUrl("/login"));
        String before = session.getId();
        mvc.perform(registration(session, TestAccounts.form())).andExpect(redirectedUrl("/cliente/envios/nuevo"));
        assertThat(session.getId()).isNotEqualTo(before);
    }

    @Test
    void aClientCannotCreateAShipmentForAnotherCustomerByTamperingWithTheForm() throws Exception {
        RegisterForm form = TestAccounts.form();
        MockHttpSession session = new MockHttpSession();
        mvc.perform(registration(session, form)).andExpect(redirectedUrl("/cliente"));
        long own = jdbc.queryForObject("select customer_id from account where email = ?", Long.class, form.getEmail());

        mvc.perform(PawayFlowTests.shipmentPost("/cliente/envios", session, "Intento de suplantación", "1"))
                .andExpect(redirectedUrl("/cliente"));

        Shipment created = shipments.search("Intento de suplantación", null, "").getFirst();
        assertThat(created.clientId()).isEqualTo(own).isNotEqualTo(1L);
        mvc.perform(get("/cliente/envios/nuevo").session(session))
                .andExpect(model().attributeDoesNotExist("clients"))
                .andExpect(content().string(not(containsString("Brayan Lopez"))));
    }

    @Test
    void duplicateEmailOrDniShowsAFieldErrorAndNeverEchoesThePassword() throws Exception {
        RegisterForm first = TestAccounts.form();
        mvc.perform(registration(new MockHttpSession(), first)).andExpect(redirectedUrl("/cliente"));
        int before = accounts();

        RegisterForm sameEmail = TestAccounts.form();
        sameEmail.setEmail(first.getEmail().toUpperCase());
        mvc.perform(registration(new MockHttpSession(), sameEmail)).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("registerForm", "email", "account.duplicate"))
                .andExpect(content().string(containsString("Este correo ya está registrado.")))
                .andExpect(content().string(not(containsString(TestAccounts.PASSWORD))));

        RegisterForm sameDocument = TestAccounts.form();
        sameDocument.setDocument(first.getDocument());
        mvc.perform(registration(new MockHttpSession(), sameDocument)).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("registerForm", "document", "account.duplicate"))
                .andExpect(content().string(containsString("Este DNI ya está registrado.")));
        assertThat(accounts()).isEqualTo(before);
    }

    @Test
    void signUpValidatesEveryFieldOnTheServer() throws Exception {
        int before = accounts();
        RegisterForm form = TestAccounts.form();
        form.setDocument("123");
        form.setPhone("abc");
        form.setEmail("no-es-correo");
        form.setPassword("corta");
        form.setPasswordConfirmation("distinta");
        mvc.perform(registration(new MockHttpSession(), form)).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registerForm", "document", "phone", "email",
                        "password", "passwordConfirmation"))
                .andExpect(content().string(containsString("Las contraseñas no coinciden.")))
                .andExpect(content().string(not(containsString("value=\"corta\""))));
        RegisterForm tooLong = TestAccounts.form();
        tooLong.setPassword("ñ".repeat(40));
        tooLong.setPasswordConfirmation("ñ".repeat(40));
        mvc.perform(registration(new MockHttpSession(), tooLong))
                .andExpect(model().attributeHasFieldErrorCode("registerForm", "password", "password.tooLong"));
        assertThat(accounts()).isEqualTo(before);
    }

    @Test
    void theConfiguredAdminSignsInToTheAdminPanelOnly() throws Exception {
        MockHttpSession admin = new MockHttpSession();
        mvc.perform(login(admin, "ADMIN@paway.test", "admin-test-password")).andExpect(redirectedUrl("/admin"));
        mvc.perform(get("/admin").session(admin)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Administrador")));
        mvc.perform(get("/cliente").session(admin)).andExpect(redirectedUrl("/login"));
        assertThat(jdbc.queryForObject("select count(*) from account where role = 'ADMIN' and email = 'admin@paway.test'",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void aClientCannotReachAdminRoutesThroughPercentEncodedPaths() throws Exception {
        MockHttpSession client = new MockHttpSession();
        mvc.perform(registration(client, TestAccounts.form())).andExpect(redirectedUrl("/cliente"));
        for (String path : new String[]{"/%61dmin", "/%61dmin/envios/1", "/ad%6Din/reclamaciones"}) {
            mvc.perform(get(URI.create(path)).session(client)).andExpect(redirectedUrl("/login"));
        }
        mvc.perform(post(URI.create("/%61dmin/envios/1/anular")).session(client)).andExpect(redirectedUrl("/login"));
        MockHttpSession admin = new MockHttpSession();
        mvc.perform(login(admin, "admin@paway.test", "admin-test-password")).andExpect(redirectedUrl("/admin"));
        mvc.perform(get(URI.create("/%63liente")).session(admin)).andExpect(redirectedUrl("/login"));
        mvc.perform(get(URI.create("/%61dmin")).session(admin)).andExpect(status().isOk());
    }
}
