package pe.edu.utp.Paway.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Customer (sender) who owns shipments. Table {@code customer}. */
@Entity
@Table(name = "customer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotNull @Pattern(regexp = "[0-9]{8}")
    @Column(nullable = false, length = 8, unique = true)
    private String document;

    @NotBlank @Email @Size(max = 150)
    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @NotNull @Pattern(regexp = "[+0-9 ()-]{7,25}")
    @Column(nullable = false, length = 25)
    private String phone;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public CustomerEntity(String fullName, String document, String email, String phone) {
        this.fullName = fullName;
        this.document = document;
        this.email = email;
        this.phone = phone;
        this.createdAt = Instant.now();
    }
}
