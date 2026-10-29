package pe.edu.utp.Paway.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Login account. Table {@code account}. A CLIENT owns one customer row; the ADMIN has none.
 * Only the BCrypt hash of the password is stored.
 */
@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEntity {
    public enum Role { CLIENT, ADMIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", unique = true)
    private Long customerId;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private AccountEntity(Long customerId, String email, String passwordHash, Role role) {
        this.customerId = customerId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public static AccountEntity client(long customerId, String email, String passwordHash) {
        return new AccountEntity(customerId, email, passwordHash, Role.CLIENT);
    }

    public static AccountEntity admin(String email, String passwordHash) {
        return new AccountEntity(null, email, passwordHash, Role.ADMIN);
    }
}
