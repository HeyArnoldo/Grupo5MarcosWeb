package pe.edu.utp.Paway.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.Paway.dto.RegisterForm;
import pe.edu.utp.Paway.entity.AccountEntity;
import pe.edu.utp.Paway.entity.AccountEntity.Role;
import pe.edu.utp.Paway.entity.CustomerEntity;
import pe.edu.utp.Paway.repository.AccountRepository;
import pe.edu.utp.Paway.repository.CustomerRepository;

/**
 * Sign-up, login and the admin bootstrap. Passwords are only ever hashed with BCrypt and
 * never logged; emails are stored trimmed and lower-cased so lookups are case-insensitive.
 */
@Service
public class AccountService {
    public static final int MIN_PASSWORD = 8;
    /** BCrypt only reads the first 72 bytes; longer input is rejected instead of truncated. */
    public static final int MAX_PASSWORD_BYTES = 72;
    private static final String ADMIN_NAME = "Administrador";

    public record SignedIn(long accountId, Role role, Long customerId, String displayName) { }

    /** The email or DNI is already registered; {@link #field()} names the form field. */
    public static class DuplicateAccountException extends RuntimeException {
        private final String field;
        public DuplicateAccountException(String field) {
            super("Duplicate " + field);
            this.field = field;
        }
        public String field() { return field; }
    }

    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    /** Compared against when the email is unknown, so both failures take the same time. */
    private final String dummyHash = encoder.encode("timing-equalizer-not-a-password");

    public AccountService(AccountRepository accounts, CustomerRepository customers) {
        this.accounts = accounts;
        this.customers = customers;
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }

    public static boolean encodable(String password) {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
    }

    @Transactional
    public SignedIn register(RegisterForm form) {
        String email = normalizeEmail(form.getEmail());
        String document = form.getDocument().strip();
        if (accounts.existsByEmail(email) || accounts.customerEmailTaken(email)) {
            throw new DuplicateAccountException("email");
        }
        if (accounts.customerDocumentTaken(document)) throw new DuplicateAccountException("document");
        validatePassword(form.getPassword());
        try {
            CustomerEntity customer = customers.saveAndFlush(new CustomerEntity(form.getFullName().strip(),
                    document, email, form.getPhone().strip()));
            AccountEntity account = accounts.saveAndFlush(
                    AccountEntity.client(customer.getId(), email, encoder.encode(form.getPassword())));
            return new SignedIn(account.getId(), Role.CLIENT, customer.getId(), customer.getFullName());
        } catch (DataIntegrityViolationException ex) {
            // A concurrent sign-up won the race between the checks above and the INSERT.
            String message = String.valueOf(ex.getMostSpecificCause().getMessage());
            throw new DuplicateAccountException(message.contains("uk_customer_document") ? "document" : "email");
        }
    }

    @Transactional(readOnly = true)
    public Optional<SignedIn> authenticate(String email, String password) {
        Optional<AccountEntity> account = accounts.findByEmail(normalizeEmail(email));
        String candidate = password != null && encodable(password) ? password : "";
        boolean matches = encoder.matches(candidate, account.map(AccountEntity::getPasswordHash).orElse(dummyHash));
        if (account.isEmpty() || !matches || candidate.isEmpty()) return Optional.empty();
        return Optional.of(view(account.get()));
    }

    @Transactional(readOnly = true)
    public Optional<SignedIn> find(long accountId) {
        return accounts.findById(accountId).map(this::view);
    }

    /**
     * Creates the ADMIN account unless it already exists; returns whether it was created.
     * Throws {@link IllegalStateException} when the address belongs to a client account.
     */
    @Transactional
    public boolean createAdminIfAbsent(String email, String password) {
        String normalized = normalizeEmail(email);
        if (normalized.isEmpty() || !normalized.contains("@")) {
            throw new IllegalArgumentException("The admin email is not a valid address.");
        }
        validatePassword(password);
        var existing = accounts.findByEmail(normalized);
        if (existing.isPresent()) {
            if (existing.get().getRole() != Role.ADMIN) {
                throw new IllegalStateException("The admin email is already registered as a client account.");
            }
            return false;
        }
        accounts.saveAndFlush(AccountEntity.admin(normalized, encoder.encode(password)));
        return true;
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD || !encodable(password)) {
            throw new IllegalArgumentException("The password must have " + MIN_PASSWORD + " to "
                    + MAX_PASSWORD_BYTES + " characters.");
        }
    }

    private SignedIn view(AccountEntity account) {
        if (account.getRole() == Role.ADMIN) {
            return new SignedIn(account.getId(), Role.ADMIN, null, ADMIN_NAME);
        }
        String name = customers.findById(account.getCustomerId()).map(CustomerEntity::getFullName).orElse("");
        return new SignedIn(account.getId(), Role.CLIENT, account.getCustomerId(), name);
    }
}
