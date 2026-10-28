package pe.edu.utp.Paway.service;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.utp.Paway.dto.ComplaintForm;
import pe.edu.utp.Paway.repository.ComplaintRepository;
import pe.edu.utp.Paway.support.PostgresTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class ComplaintServiceTests {
    @Autowired ComplaintService service;
    @Autowired ComplaintRepository repository;

    private static ComplaintForm form() {
        ComplaintForm form = new ComplaintForm();
        form.setName(" Consumidora ");
        form.setDocument("CE-123456");
        form.setAddress("Jr. Lima 123");
        form.setEmail("consumidora@example.com");
        form.setPhone("987654321");
        form.setAmount("25.5");
        form.setGuide("PW-2026-0001");
        form.setDescription("Envío a Huaral");
        form.setDetail("No llegó a tiempo.");
        form.setRequest("Devolución del flete.");
        return form;
    }

    @Test
    void registeringTheSameTokenTwiceKeepsOneSheet() {
        String token = UUID.randomUUID().toString();
        long before = repository.count();

        ComplaintService.Sheet first = service.register(form(), token);
        ComplaintService.Sheet again = service.register(form(), token);

        assertThat(again.number()).isEqualTo(first.number()).matches("PW-WEB-[0-9]{6}");
        assertThat(repository.count()).isEqualTo(before + 1);
        assertThat(first.details()).containsEntry("name", "Consumidora").containsEntry("amount", "25.50")
                .containsEntry("minor", "false").containsEntry("guardian", "").containsEntry("type", "RECLAMO");
        assertThat(first.response()).isEmpty();
        assertThat(service.all()).extracting(ComplaintService.Sheet::token).contains(token);
    }

    @Test
    void sheetNumbersAreCorrelative() {
        long a = Long.parseLong(service.register(form(), UUID.randomUUID().toString()).number().substring(7));
        long b = Long.parseLong(service.register(form(), UUID.randomUUID().toString()).number().substring(7));
        assertThat(b).isGreaterThan(a);
    }

    @Test
    void respondingIsStoredAndUnknownTokensAreNotFound() {
        String token = UUID.randomUUID().toString();
        service.register(form(), token);

        service.respond(token, " Se devolvió el flete. ", " Correo 123 ");

        ComplaintService.Sheet sheet = service.get(token);
        assertThat(sheet.response()).isEqualTo("Se devolvió el flete.");
        assertThat(sheet.delivery()).isEqualTo("Correo 123");
        assertThat(sheet.responseDate()).endsWith("(Perú)");
        assertThatThrownBy(() -> service.get(UUID.randomUUID().toString()))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.get("not-a-token")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.respond("not-a-token", "x", "y"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
