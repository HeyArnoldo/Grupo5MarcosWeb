package pe.edu.utp.Paway.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import pe.edu.utp.Paway.dto.RegisterForm;
import pe.edu.utp.Paway.entity.AccountEntity.Role;
import pe.edu.utp.Paway.support.PostgresTestConfiguration;
import pe.edu.utp.Paway.support.TestAccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Not @Transactional on purpose: every call commits, like a real request. */
@SpringBootTest
@Import(PostgresTestConfiguration.class)
class AccountServiceTests {
    @Autowired AccountService service;
    @Autowired JdbcTemplate jdbc;

    private int count(String table) {
        return jdbc.queryForObject("select count(*) from " + table, Integer.class);
    }

    @Test
    void registerCreatesTheCustomerAndAClientAccountWithAHashedPassword() {
        RegisterForm form = TestAccounts.form();
        String email = form.getEmail();
        form.setEmail("  " + email.toUpperCase() + " ");

        AccountService.SignedIn signedIn = service.register(form);

        assertThat(signedIn.role()).isEqualTo(Role.CLIENT);
        assertThat(signedIn.customerId()).isNotNull();
        assertThat(signedIn.displayName()).isEqualTo(form.getFullName());
        assertThat(jdbc.queryForObject("select document from customer where id = ?", String.class,
                signedIn.customerId())).isEqualTo(form.getDocument());
        String storedEmail = jdbc.queryForObject("select email from account where id = ?", String.class,
                signedIn.accountId());
        assertThat(storedEmail).isEqualTo(email);
        assertThat(jdbc.queryForObject("select email from customer where id = ?", String.class,
                signedIn.customerId())).isEqualTo(email);
        String hash = jdbc.queryForObject("select password_hash from account where id = ?", String.class,
                signedIn.accountId());
        assertThat(hash).isNotEqualTo(form.getPassword()).doesNotContain(form.getPassword()).startsWith("$2");
    }

    @Test
    void registerRejectsADuplicateEmailIgnoringCaseWithoutWritingRows() {
        RegisterForm first = TestAccounts.form();
        service.register(first);
        int customers = count("customer");
        int accounts = count("account");

        RegisterForm again = TestAccounts.form();
        again.setEmail(first.getEmail().toUpperCase());

        assertThatThrownBy(() -> service.register(again))
                .isInstanceOfSatisfying(AccountService.DuplicateAccountException.class,
                        ex -> assertThat(ex.field()).isEqualTo("email"));
        assertThat(count("customer")).isEqualTo(customers);
        assertThat(count("account")).isEqualTo(accounts);
    }

    @Test
    void registerRejectsAnEmailOrDniAlreadyUsedByADemoCustomer() {
        RegisterForm demoEmail = TestAccounts.form();
        demoEmail.setEmail("Brayan@Example.com");
        assertThatThrownBy(() -> service.register(demoEmail))
                .isInstanceOfSatisfying(AccountService.DuplicateAccountException.class,
                        ex -> assertThat(ex.field()).isEqualTo("email"));

        RegisterForm demoDocument = TestAccounts.form();
        demoDocument.setDocument("12345678");
        assertThatThrownBy(() -> service.register(demoDocument))
                .isInstanceOfSatisfying(AccountService.DuplicateAccountException.class,
                        ex -> assertThat(ex.field()).isEqualTo("document"));
    }

    @Test
    void authenticateAcceptsTheRightPasswordAndRejectsWrongPasswordsAndUnknownEmails() {
        RegisterForm form = TestAccounts.form();
        AccountService.SignedIn registered = service.register(form);

        assertThat(service.authenticate(" " + form.getEmail().toUpperCase(), form.getPassword()))
                .hasValueSatisfying(s -> assertThat(s.accountId()).isEqualTo(registered.accountId()));
        assertThat(service.authenticate(form.getEmail(), form.getPassword() + "x")).isEmpty();
        assertThat(service.authenticate("nobody-" + form.getEmail(), form.getPassword())).isEmpty();
        assertThat(service.authenticate(form.getEmail(), "")).isEmpty();
        assertThat(service.authenticate(form.getEmail(), "ñ".repeat(72))).isEmpty();
    }

    @Test
    void findReturnsTheSignedInViewOrEmpty() {
        AccountService.SignedIn registered = service.register(TestAccounts.form());
        assertThat(service.find(registered.accountId())).contains(registered);
        assertThat(service.find(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void createsTheAdminOnlyOnceAndRejectsShortPasswords() {
        String email = "Root-" + TestAccounts.unique() + "@paway.test";
        assertThat(service.createAdminIfAbsent(email, "admin-password-1")).isTrue();
        assertThat(service.createAdminIfAbsent(email.toLowerCase(), "another-password")).isFalse();
        assertThat(service.authenticate(email, "admin-password-1"))
                .hasValueSatisfying(s -> {
                    assertThat(s.role()).isEqualTo(Role.ADMIN);
                    assertThat(s.customerId()).isNull();
                    assertThat(s.displayName()).isEqualTo("Administrador");
                });
        assertThatThrownBy(() -> service.createAdminIfAbsent("short-" + email, "1234567"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesToCreateTheAdminWhenTheEmailBelongsToAClient() {
        RegisterForm form = TestAccounts.form();
        service.register(form);
        assertThatThrownBy(() -> service.createAdminIfAbsent(form.getEmail(), "admin-password-1"))
                .isInstanceOf(IllegalStateException.class);
    }
}
