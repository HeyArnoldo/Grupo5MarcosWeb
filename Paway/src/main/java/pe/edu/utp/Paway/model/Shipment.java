package pe.edu.utp.Paway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Shipment(long id, String guide, long clientId, String recipient, String document,
        String origin, String province, String district, String category, BigDecimal weight,
        int quantity, BigDecimal total, ShipmentStatus status, String observation,
        LocalDateTime createdAt, List<Event> events) {

    public Shipment {
        events = List.copyOf(events);
    }

    public record Event(ShipmentStatus status, LocalDateTime date, String observation) {
    }
}
