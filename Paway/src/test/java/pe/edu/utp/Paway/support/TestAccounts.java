package pe.edu.utp.Paway.support;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import pe.edu.utp.Paway.dto.RegisterForm;

/** Unique sign-up data, so tests sharing one database never collide on email or DNI. */
public final class TestAccounts {
    public static final String PASSWORD = "clave-segura-123";
    // DNIs 4xxxxxxx are never used by the seed data or the other tests.
    private static final AtomicInteger SEQUENCE =
            new AtomicInteger(40_000_000 + ThreadLocalRandom.current().nextInt(5_000_000) * 2);

    private TestAccounts() { }

    public static String unique() {
        return String.valueOf(SEQUENCE.incrementAndGet());
    }

    public static RegisterForm form() {
        String id = unique();
        RegisterForm form = new RegisterForm();
        form.setFullName("Cliente " + id);
        form.setDocument(id);
        form.setEmail("cliente" + id + "@example.com");
        form.setPhone("987654321");
        form.setPassword(PASSWORD);
        form.setPasswordConfirmation(PASSWORD);
        return form;
    }
}
