package pe.edu.utp.Paway.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Sheet of the Libro de Reclamaciones. Table {@code complaint}. The {@code token} is the
 * one-time submission token of the form, which makes a double submit idempotent.
 */
@Entity
@Table(name = "complaint")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComplaintEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true, updatable = false)
    private UUID token;

    @NotNull @Pattern(regexp = "PW-WEB-[0-9]{6,}")
    @Column(name = "sheet_number", nullable = false, length = 20, unique = true, updatable = false)
    private String sheetNumber;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotBlank @Size(max = 150)
    @Column(name = "consumer_name", nullable = false, length = 150)
    private String consumerName;

    @NotNull @Pattern(regexp = "[A-Za-z0-9-]{6,20}")
    @Column(name = "consumer_document", nullable = false, length = 20)
    private String consumerDocument;

    @NotBlank @Size(max = 250)
    @Column(nullable = false, length = 250)
    private String address;

    @NotBlank @Email @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String email;

    @NotNull @Pattern(regexp = "[+0-9 ()-]{7,25}")
    @Column(nullable = false, length = 25)
    private String phone;

    @Column(nullable = false)
    private boolean minor;

    @NotNull @Size(max = 150)
    @Column(name = "guardian_name", nullable = false, length = 150)
    private String guardianName = "";

    @NotNull @Size(max = 20)
    @Column(name = "guardian_document", nullable = false, length = 20)
    private String guardianDocument = "";

    @NotNull @Size(max = 250)
    @Column(name = "guardian_contact", nullable = false, length = 250)
    private String guardianContact = "";

    @NotNull @Pattern(regexp = "RECLAMO|QUEJA")
    @Column(name = "complaint_type", nullable = false, length = 10)
    private String complaintType;

    @NotNull @Pattern(regexp = "SERVICIO|PRODUCTO")
    @Column(name = "item_type", nullable = false, length = 10)
    private String itemType;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String guide = "";

    @NotBlank @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String description;

    @NotBlank @Size(max = 4000)
    @Column(nullable = false, length = 4000)
    private String detail;

    @NotBlank @Size(max = 2000)
    @Column(name = "consumer_request", nullable = false, length = 2000)
    private String request;

    @NotNull @Pattern(regexp = "EMAIL|DOMICILIO")
    @Column(name = "response_channel", nullable = false, length = 10)
    private String responseChannel;

    @Size(max = 4000)
    @Column(length = 4000)
    private String response;

    @Size(max = 500)
    @Column(name = "response_delivery", length = 500)
    private String responseDelivery;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public ComplaintEntity(UUID token, String sheetNumber, Instant createdAt) {
        this.token = token;
        this.sheetNumber = sheetNumber;
        this.createdAt = createdAt;
    }

    /** Consumer data, set once when the sheet is registered. */
    public void fill(String consumerName, String consumerDocument, String address, String email, String phone,
            boolean minor, String guardianName, String guardianDocument, String guardianContact) {
        this.consumerName = consumerName;
        this.consumerDocument = consumerDocument;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.minor = minor;
        this.guardianName = guardianName;
        this.guardianDocument = guardianDocument;
        this.guardianContact = guardianContact;
    }

    /** Claim data, set once when the sheet is registered. */
    public void describe(String complaintType, String itemType, BigDecimal amount, String guide,
            String description, String detail, String request, String responseChannel) {
        this.complaintType = complaintType;
        this.itemType = itemType;
        this.amount = amount;
        this.guide = guide;
        this.description = description;
        this.detail = detail;
        this.request = request;
        this.responseChannel = responseChannel;
    }

    public void respond(String response, String delivery, Instant at) {
        this.response = response;
        this.responseDelivery = delivery;
        this.respondedAt = at;
    }

    @AssertTrue(message = "guardian data is required for minors")
    boolean isGuardianComplete() {
        return !minor || (guardianName != null && !guardianName.isBlank()
                && guardianDocument != null && guardianDocument.matches("[A-Za-z0-9-]{6,20}")
                && guardianContact != null && !guardianContact.isBlank());
    }
}
