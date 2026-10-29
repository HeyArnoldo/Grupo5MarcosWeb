package pe.edu.utp.Paway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import pe.edu.utp.Paway.repository.CustomerRepository;
import pe.edu.utp.Paway.repository.ShipmentRepository;
import pe.edu.utp.Paway.dto.ShipmentForm;
import pe.edu.utp.Paway.service.ShipmentService;

@SpringBootTest
@ActiveProfiles("local")
class LocalProfileTests {
    @Autowired
    private CustomerRepository customers;

    @Autowired
    private ShipmentRepository shipments;

    @Autowired
    private ShipmentService shipmentService;

    @Test
    void startsWithDemoCustomersAndShipments() {
        assertEquals(2, customers.count());
        assertEquals(4, shipments.count());

        ShipmentForm form = new ShipmentForm();
        form.setRecipient("Destino de prueba");
        form.setDocument("12345678");
        form.setProvince("lima");
        form.setDistrict("Miraflores");
        var shipment = shipmentService.create(form);

        assertEquals(5, shipments.count());
        assertTrue(shipment.guide().endsWith("-0005"));
    }
}