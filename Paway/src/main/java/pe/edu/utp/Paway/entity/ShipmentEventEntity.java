package pe.edu.utp.Paway.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.utp.Paway.model.ShipmentStatus;

/** One entry of a shipment's tracking timeline. Table {@code shipment_event}; created only by {@link ShipmentEntity}. */
@Entity
@Table(name = "shipment_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false, updatable = false)
    private ShipmentEntity shipment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private ShipmentStatus status;

    @NotNull
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @NotNull @Size(max = 500)
    @Column(nullable = false, length = 500, updatable = false)
    private String observation;

    ShipmentEventEntity(ShipmentEntity shipment, ShipmentStatus status, Instant occurredAt, String observation) {
        this.shipment = shipment;
        this.status = status;
        this.occurredAt = occurredAt;
        this.observation = observation;
    }
}
