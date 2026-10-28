package pe.edu.utp.Paway.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.Paway.entity.ShipmentEntity;
import pe.edu.utp.Paway.entity.ShipmentEventEntity;
import pe.edu.utp.Paway.model.ShipmentStatus;
import pe.edu.utp.Paway.support.PostgresTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class ShipmentRepositoryTests {
    private static final String YEAR = String.valueOf(Year.now(ZoneId.of("America/Lima")).getValue());

    @Autowired ShipmentRepository shipments;
    @Autowired CustomerRepository customers;
    @Autowired AgencyRepository agencies;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;

    private ShipmentEntity newShipment(String guide, String document) {
        ShipmentEntity.Details details = new ShipmentEntity.Details("Repositorio", document, "lima", "Miraflores",
                "textil", new BigDecimal("1.00"), 1, "");
        return ShipmentEntity.register(shipments.nextId(), guide, customers.findById(1L).orElseThrow(),
                agencies.findByName("Lima Centro").orElseThrow(), details, new BigDecimal("10.00"), Instant.now());
    }

    @Test
    void seedMigrationLoadsTheReadmeDemoData() {
        assertThat(customers.findAllByOrderByIdAsc()).extracting(c -> c.getFullName())
                .containsExactly("Brayan Lopez", "María Quispe");
        ShipmentEntity first = shipments.findByGuide("PW-" + YEAR + "-0001").orElseThrow();
        assertThat(first.getRecipient()).isEqualTo("Carlos Mendoza");
        assertThat(first.getStatus()).isEqualTo(ShipmentStatus.TRANSIT);
        assertThat(first.getTotal()).isEqualByComparingTo("20.00");
        assertThat(first.getEvents()).extracting(ShipmentEventEntity::getStatus)
                .containsExactly(ShipmentStatus.REGISTERED, ShipmentStatus.TRANSIT);
        assertThat(shipments.findByGuide("PW-" + YEAR + "-0004").orElseThrow().getStatus())
                .isEqualTo(ShipmentStatus.REGISTERED);
        assertThat(shipments.nextId()).isGreaterThan(4);
    }

    @Test
    void guideNumberIsUnique() {
        ShipmentEntity duplicate = newShipment("PW-" + YEAR + "-0001", "12345678");
        assertThatThrownBy(() -> shipments.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void eventsAreReadBackInTimelineOrder() {
        ShipmentEntity shipment = newShipment("PW-" + YEAR + "-9001", "12345678");
        Instant registered = shipment.getCreatedAt();
        shipment.changeStatus(ShipmentStatus.TRANSIT, "Salida a destino.", registered.plusSeconds(120));
        shipment.changeStatus(ShipmentStatus.WAREHOUSE, "Llegó antes al almacén.", registered.plusSeconds(60));
        shipments.saveAndFlush(shipment);
        entityManager.clear();

        ShipmentEntity reloaded = shipments.findByGuide(shipment.getGuide()).orElseThrow();
        assertThat(reloaded.getEvents()).extracting(ShipmentEventEntity::getStatus)
                .containsExactly(ShipmentStatus.REGISTERED, ShipmentStatus.WAREHOUSE, ShipmentStatus.TRANSIT);
        assertThat(reloaded.getVersion()).isNotNull();
    }

    @Test
    void beanValidationRejectsInvalidEntitiesBeforeReachingTheDatabase() {
        ShipmentEntity invalid = newShipment("PW-" + YEAR + "-9002", "123");
        assertThatThrownBy(() -> shipments.saveAndFlush(invalid))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("recipientDocument");
    }

    @Test
    void databaseCheckConstraintsMirrorTheValidationRules() {
        assertThatThrownBy(() -> jdbc.update("""
                insert into shipment (guide, customer_id, origin_agency_id, recipient, recipient_document,
                    province, district, category, weight_kg, quantity, total, status, observation,
                    created_at, updated_at)
                values ('PW-2000-9003', 1, 1, 'Sin paquetes', '12345678', 'lima', 'Surco', 'textil',
                    1.00, 0, 10.00, 'REGISTERED', '', now(), now())
                """)).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_shipment_quantity");
    }
}
