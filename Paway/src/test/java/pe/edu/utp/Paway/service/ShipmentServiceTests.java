package pe.edu.utp.Paway.service;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import pe.edu.utp.Paway.dto.ShipmentForm;
import pe.edu.utp.Paway.entity.ShipmentEntity;
import pe.edu.utp.Paway.model.Shipment;
import pe.edu.utp.Paway.model.ShipmentStatus;
import pe.edu.utp.Paway.repository.ShipmentRepository;
import pe.edu.utp.Paway.support.PostgresTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Not @Transactional on purpose: every call commits, like a real request. */
@SpringBootTest
@Import(PostgresTestConfiguration.class)
class ShipmentServiceTests {
    @Autowired ShipmentService service;
    @Autowired ShipmentRepository repository;
    @Autowired PlatformTransactionManager transactionManager;

    private static ShipmentForm form(String recipient) {
        ShipmentForm form = new ShipmentForm();
        form.setClientId(2);
        form.setRecipient(recipient);
        form.setDocument("87654321");
        form.setOrigin("Huaral Centro");
        form.setProvince("lima");
        form.setDistrict("Lince");
        form.setCategory("tecnologia");
        form.setWeight(new java.math.BigDecimal("3.00"));
        form.setQuantity(2);
        form.setObservation(" Frágil ");
        return form;
    }

    @Test
    void createPersistsGuideFromTheSequenceAndTheRegisteredEvent() {
        Shipment created = service.create(form("Servicio crea"));

        Shipment stored = service.get(created.id());
        int year = Year.now(ZoneId.of("America/Lima")).getValue();
        assertThat(stored.guide()).isEqualTo("PW-%d-%04d".formatted(year, created.id()));
        assertThat(stored.clientId()).isEqualTo(2);
        assertThat(stored.origin()).isEqualTo("Huaral Centro");
        assertThat(stored.total()).isEqualByComparingTo("36.00");
        assertThat(stored.observation()).isEqualTo("Frágil");
        assertThat(stored.events()).singleElement().satisfies(e -> {
            assertThat(e.status()).isEqualTo(ShipmentStatus.REGISTERED);
            assertThat(e.observation()).isEqualTo("Frágil");
        });
        assertThat(service.track(" " + stored.guide().toLowerCase() + " ")).contains(stored);
        assertThat(service.forClient(2)).extracting(Shipment::id).contains(created.id());
        assertThat(service.forClient(1)).extracting(Shipment::id).doesNotContain(created.id());
    }

    @Test
    void cancellingKeepsTheHistoryAndForbidsLaterChanges() {
        long id = service.create(form("Servicio anula")).id();
        service.changeStatus(id, ShipmentStatus.TRANSIT, "Salida a destino.");
        service.changeStatus(id, ShipmentStatus.CANCELLED, "Anulado por el cliente.");

        assertThatThrownBy(() -> service.update(id, form("Editado tras anular")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("anulado");
        assertThatThrownBy(() -> service.changeStatus(id, ShipmentStatus.DELIVERED, ""))
                .isInstanceOf(IllegalArgumentException.class);

        Shipment stored = service.get(id);
        assertThat(stored.status()).isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(stored.recipient()).isEqualTo("Servicio anula");
        assertThat(stored.events()).extracting(Shipment.Event::status).containsExactly(
                ShipmentStatus.REGISTERED, ShipmentStatus.TRANSIT, ShipmentStatus.CANCELLED);
    }

    @Test
    void rejectsInvalidDataWithoutPersistingAnything() {
        long before = repository.count();
        ShipmentForm sameDistrict = form("Mismo distrito");
        sameDistrict.setOrigin("Lima Centro");
        sameDistrict.setDistrict("Lima Centro");
        ShipmentForm badDocument = form("Documento inválido");
        badDocument.setDocument("12AB");
        ShipmentForm unknownCustomer = form("Cliente inexistente");
        unknownCustomer.setClientId(999);

        assertThatThrownBy(() -> service.create(sameDistrict)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(badDocument)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(unknownCustomer)).isInstanceOf(IllegalArgumentException.class);
        assertThat(repository.count()).isEqualTo(before);
    }

    @Test
    void staleEditFormIsRejectedByTheVersion() {
        long id = service.create(form("Versión")).id();
        ShipmentForm openedEarlier = ShipmentForm.from(service.get(id));
        service.changeStatus(id, ShipmentStatus.WAREHOUSE, "Otro operador movió el envío.");

        openedEarlier.setRecipient("Cambio con datos viejos");
        assertThatThrownBy(() -> service.update(id, openedEarlier))
                .isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(service.get(id).recipient()).isEqualTo("Versión");
    }

    @Test
    void concurrentTransactionsOnTheSameShipmentConflict() {
        long id = service.create(form("Concurrencia")).id();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThatThrownBy(() -> tx.executeWithoutResult(first -> {
            ShipmentEntity mine = repository.findById(id).orElseThrow();
            mine.getEvents().size();
            tx.executeWithoutResult(second -> repository.findById(id).orElseThrow()
                    .changeStatus(ShipmentStatus.TRANSIT, "Ganó el otro operador.", Instant.now()));
            mine.changeStatus(ShipmentStatus.INCIDENT, "Llegó tarde.", Instant.now());
        })).isInstanceOf(OptimisticLockingFailureException.class);

        Shipment stored = service.get(id);
        assertThat(stored.status()).isEqualTo(ShipmentStatus.TRANSIT);
        assertThat(stored.events()).hasSize(2);
    }
}
