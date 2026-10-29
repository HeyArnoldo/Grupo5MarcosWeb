package pe.edu.utp.Paway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import pe.edu.utp.Paway.service.AccountService;

/**
 * Creates the ADMIN account from {@code PAWAY_ADMIN_EMAIL} / {@code PAWAY_ADMIN_PASSWORD} on
 * startup when both are set and that email has no account yet. There is no default password:
 * without the variables the application starts with no admin. An invalid value stops startup.
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);
    private final AccountService accounts;
    private final String email;
    private final String password;

    public AdminAccountInitializer(AccountService accounts, @Value("${paway.admin.email:}") String email,
            @Value("${paway.admin.password:}") String password) {
        this.accounts = accounts;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isEmpty()) {
            log.warn("No admin account configured: set PAWAY_ADMIN_EMAIL and PAWAY_ADMIN_PASSWORD to create one.");
            return;
        }
        try {
            if (accounts.createAdminIfAbsent(email, password)) log.info("Admin account created.");
        } catch (IllegalStateException ex) {
            log.error("{} Choose another PAWAY_ADMIN_EMAIL; no admin account was created.", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid PAWAY_ADMIN_EMAIL or PAWAY_ADMIN_PASSWORD (password needs "
                    + AccountService.MIN_PASSWORD + " to " + AccountService.MAX_PASSWORD_BYTES + " characters).");
        }
    }
}
