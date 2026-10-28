package pe.edu.utp.Paway.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.utp.Paway.model.ShipmentStatus;

/**
 * Shipment aggregate: owns its tracking timeline ({@link ShipmentEventEntity}) and enforces
 * the business rules that used to live in the in-memory service (a cancelled shipment keeps
 * its history and cannot change). {@link Version} gives optimistic locking.
 */
@Entity
@Table(name = "shipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentEntity {
    /** Assigned from {@code shipment_id_seq} before persisting, because the guide is derived from it. */
    @Id
    private Long id;

    @NotNull @Pattern(regexp = "PW-[0-9]{4}-[0-9]{4,}")
    @Column(nullable = false, length = 20, unique = true, updatable = false)
    private String guide;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_agency_id", nullable = false)
    private AgencyEntity originAgency;

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String recipient;

    @NotNull @Pattern(regexp = "[0-9]{8}")
    @Column(name = "recipient_document", nullable = false, length = 8)
    private String recipientDocument;

    @NotNull @Pattern(regexp = "lima|huarochiri|huaral|canete")
    @Column(nullable = false, length = 20)
    private String province;

    @NotBlank @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String district;

    @NotNull @Pattern(regexp = "textil|otros|hogar_higiene|tecnologia|joyeria")
    @Column(nullable = false, length = 20)
    private String category;

    @NotNull @DecimalMin("0.01") @DecimalMax("1000") @Digits(integer = 4, fraction = 2)
    @Column(name = "weight_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    @NotNull @Min(1) @Max(100)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @NotNull @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String observation;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @Valid
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt ASC, id ASC")
    private List<ShipmentEventEntity> events = new ArrayList<>();

    /** Editable data of a shipment (what the form captures), already validated against the catalog. */
    public record Details(String recipient, String document, String province, String district,
            String category, BigDecimal weight, int quantity, String observation) { }

    public static ShipmentEntity register(long id, String guide, CustomerEntity customer, AgencyEntity origin,
            Details details, BigDecimal total, Instant now) {
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.id = id;
        shipment.guide = guide;
        shipment.status = ShipmentStatus.REGISTERED;
        shipment.createdAt = now;
        shipment.apply(customer, origin, details, total, now);
        shipment.events.add(new ShipmentEventEntity(shipment, ShipmentStatus.REGISTERED, now, shipment.observation));
        return shipment;
    }

    public void update(CustomerEntity customer, AgencyEntity origin, Details details, BigDecimal total, Instant now) {
        requireActive();
        apply(customer, origin, details, total, now);
    }

    /**
     * Appends a timeline event. Returns {@code false} (and records nothing) when the status does not change.
     */
    public boolean changeStatus(ShipmentStatus newStatus, String note, Instant at) {
        requireActive();
        if (newStatus == null || note == null || note.length() > 500) {
            throw new IllegalArgumentException("Revisa el estado y la observación (máximo 500 caracteres).");
        }
        if (status == newStatus) return false;
        String text = note.strip();
        events.add(new ShipmentEventEntity(this, newStatus, at, text));
        status = newStatus;
        observation = text;
        updatedAt = at;
        return true;
    }

    public boolean isCancelled() { return status == ShipmentStatus.CANCELLED; }

    public List<ShipmentEventEntity> getEvents() { return Collections.unmodifiableList(events); }

    private void requireActive() {
        if (isCancelled()) {
            throw new IllegalArgumentException("Un envío anulado conserva su historial y no se puede modificar.");
        }
    }

    private void apply(CustomerEntity customer, AgencyEntity origin, Details details, BigDecimal total, Instant now) {
        this.customer = customer;
        this.originAgency = origin;
        this.recipient = details.recipient() == null ? null : details.recipient().strip();
        this.recipientDocument = details.document();
        this.province = details.province();
        this.district = details.district();
        this.category = details.category();
        this.weightKg = details.weight();
        this.quantity = details.quantity();
        this.observation = details.observation() == null ? "" : details.observation().strip();
        this.total = total;
        this.updatedAt = now;
    }
}
