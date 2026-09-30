package pe.edu.utp.Paway.controller;

import java.time.Year;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import pe.edu.utp.Paway.model.Shipment;
import pe.edu.utp.Paway.model.ShipmentStatus;
import pe.edu.utp.Paway.service.ShipmentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PawayFlowTests {
    @Autowired private MockMvc mvc;
    @Autowired private ShipmentService shipments;

    private MockHttpSession profile(String profile) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/login").session(session).param("profile", profile))
                .andExpect(status().is3xxRedirection());
        return session;
    }

    private MockHttpServletRequestBuilder shipmentPost(String path, MockHttpSession session, String recipient) {
        return post(path).session(session).param("clientId", "1")
                .param("recipient", recipient).param("document", "12345678")
                .param("origin", "Lima Centro").param("province", "lima")
                .param("district", "Miraflores").param("category", "tecnologia")
                .param("weight", "2.5").param("quantity", "3").param("observation", "Prueba del flujo integrado.");
    }

    @Test
    void rendersEveryModuleWithSharedLayoutAndStaticAssets() throws Exception {
        for (String route : new String[]{"/", "/cotizar", "/rastrear", "/login"}) {
            mvc.perform(get(route)).andExpect(status().isOk())
                    .andExpect(content().string(containsString("/css/paway.css")));
        }
        MockHttpSession admin = profile("admin");
        for (String route : new String[]{"/admin", "/admin/envios/nuevo", "/admin/envios/1", "/admin/envios/1/editar"}) {
            mvc.perform(get(route).session(admin)).andExpect(status().isOk());
        }
        MockHttpSession client = profile("client1");
        mvc.perform(get("/cliente").session(client)).andExpect(status().isOk());
        mvc.perform(get("/cliente/envios/nuevo").session(client)).andExpect(status().isOk());
        mvc.perform(get("/css/paway.css")).andExpect(status().isOk());
        mvc.perform(get("/js/paway.js")).andExpect(status().isOk());
    }

    @Test
    void createsEditsTracksAndCancelsTheSameShipment() throws Exception {
        MockHttpSession admin = profile("admin");
        mvc.perform(shipmentPost("/admin/envios", admin, "Flujo integrado"))
                .andExpect(redirectedUrl("/admin"));
        Shipment shipment = shipments.search("Flujo integrado", null, "").getFirst();
        assertThat(shipment.total()).isEqualByComparingTo("49.50");
        assertThat(shipment.guide()).startsWith("PW-" + Year.now().getValue() + "-");

        mvc.perform(shipmentPost("/admin/envios/" + shipment.id(), admin, "Destinatario actualizado"))
                .andExpect(redirectedUrl("/admin"));
        assertThat(shipments.get(shipment.id()).guide()).isEqualTo(shipment.guide());
        assertThat(shipments.get(shipment.id()).recipient()).isEqualTo("Destinatario actualizado");

        mvc.perform(post("/admin/envios/" + shipment.id() + "/estado").session(admin)
                .param("status", "TRANSIT").param("observation", "Salida a destino."))
                .andExpect(redirectedUrl("/admin/envios/" + shipment.id()));
        mvc.perform(get("/rastrear").param("guide", " " + shipment.guide().toLowerCase() + " "))
                .andExpect(status().isOk()).andExpect(content().string(containsString("En tránsito")))
                .andExpect(content().string(containsString("Salida a destino.")));
        mvc.perform(get("/cliente").session(profile("client1")))
                .andExpect(content().string(containsString(shipment.guide())));

        mvc.perform(post("/admin/envios/" + shipment.id() + "/anular").session(admin))
                .andExpect(redirectedUrl("/admin"));
        assertThat(shipments.get(shipment.id()).status()).isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(shipments.get(shipment.id()).events()).hasSize(3);
        mvc.perform(get("/rastrear").param("guide", shipment.guide()))
                .andExpect(content().string(containsString("Anulado")));
        mvc.perform(get("/admin/envios/" + shipment.id() + "/editar").session(admin))
                .andExpect(status().isConflict());
        mvc.perform(post("/admin/envios/" + shipment.id() + "/estado").session(admin).param("status", "REGISTERED"))
                .andExpect(flash().attributeExists("error"));
        assertThat(shipments.get(shipment.id()).status()).isEqualTo(ShipmentStatus.CANCELLED);
    }

    @Test
    void retainsQuoteAcrossDemoLoginAndUsesTheSessionClient() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/cotizar").session(session).param("province", "canete").param("district", "Asia")
                .param("category", "joyeria").param("weight", "1").param("quantity", "2")
                .param("requestShipment", "true")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/login").session(session).param("profile", "client2"))
                .andExpect(redirectedUrl("/cliente/envios/nuevo"));
        mvc.perform(get("/cliente/envios/nuevo").session(session))
                .andExpect(status().isOk()).andExpect(content().string(containsString("value=\"Asia\" selected=\"selected\"")));
        mvc.perform(shipmentPost("/cliente/envios", session, "Cliente de la sesión"))
                .andExpect(redirectedUrl("/cliente"));
        Shipment created = shipments.search("Cliente de la sesión", null, "").getFirst();
        assertThat(created.clientId()).isEqualTo(2);
        assertThat(session.getAttribute("pendingQuote")).isNull();
        mvc.perform(get("/cliente").session(profile("client1")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString(created.guide()))));
    }

    @Test
    void validatesOnServerAndDoesNotCreateInvalidShipments() throws Exception {
        MockHttpSession admin = profile("admin");
        int before = shipments.search("", null, "").size();
        mvc.perform(shipmentPost("/admin/envios", admin, "Invalid weight").with(request -> {
            request.setParameter("weight", "-1");
            return request;
        }))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("shipmentForm", "weight"));
        mvc.perform(shipmentPost("/admin/envios", admin, "Invalid destination").with(request -> {
            request.setParameter("district", "Asia");
            return request;
        }))
                .andExpect(status().isOk()).andExpect(model().attributeHasErrors("shipmentForm"));
        mvc.perform(post("/cotizar").param("province", "lima").param("district", "Miraflores")
                .param("category", "textil").param("weight", "abc").param("quantity", "1"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Ingresa un peso numérico válido.")));
        assertThat(shipments.search("", null, "")).hasSize(before);
    }

    @Test
    void restrictsPanelsToTheirDemoProfileAndLogsOut() throws Exception {
        mvc.perform(get("/admin")).andExpect(redirectedUrl("/login"));
        MockHttpSession client = profile("client1");
        mvc.perform(get("/admin").session(client)).andExpect(redirectedUrl("/login"));
        mvc.perform(shipmentPost("/admin/envios", client, "Not allowed"))
                .andExpect(redirectedUrl("/login"));
        mvc.perform(get("/cliente").session(profile("admin"))).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/logout").session(client)).andExpect(redirectedUrl("/"));
        assertThat(client.isInvalid()).isTrue();
        mvc.perform(post("/login").param("profile", "unknown")).andExpect(flash().attributeExists("error"));
    }

    @Test
    void distinguishesMissingGuidesAndProvidesDestinationApi() throws Exception {
        mvc.perform(get("/rastrear").param("guide", "FAKE-12345"))
                .andExpect(content().string(containsString("No encontramos un envío con esa guía.")))
                .andExpect(model().attributeDoesNotExist("shipment"));
        mvc.perform(get("/api/v1/destinos/huaral/distritos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[1]").value("Chancay"));
        mvc.perform(get("/api/v1/destinos/unknown/distritos")).andExpect(status().isNotFound());
        mvc.perform(get("/admin/envios/999999").session(profile("admin"))).andExpect(status().isNotFound());
    }

    @Test
    void filtersAndPaginatesAdministrativeResults() throws Exception {
        MockHttpSession admin = profile("admin");
        for (int i = 0; i < 6; i++) {
            mvc.perform(shipmentPost("/admin/envios", admin, "Paginación " + i)).andExpect(redirectedUrl("/admin"));
        }
        mvc.perform(get("/admin").session(admin).param("q", "Paginación").param("page", "2"))
                .andExpect(status().isOk()).andExpect(model().attribute("resultCount", 6))
                .andExpect(model().attribute("pages", 2)).andExpect(model().attribute("page", 2));
        mvc.perform(get("/admin").session(admin).param("q", "Paginación").param("status", "DELIVERED"))
                .andExpect(status().isOk()).andExpect(model().attribute("resultCount", 0));
    }
}
