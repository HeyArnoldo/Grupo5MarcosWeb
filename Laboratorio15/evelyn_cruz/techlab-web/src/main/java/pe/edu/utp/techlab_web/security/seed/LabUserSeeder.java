package pe.edu.utp.techlab_web.security.seed;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.techlab_web.security.model.UserAccount;
import pe.edu.utp.techlab_web.security.repository.UserAccountRepository;

@Component
@Profile("lab")
public class LabUserSeeder implements ApplicationRunner {

    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final String adminName;
    private final String adminPassword;
    private final String studentName;
    private final String studentPassword;

    public LabUserSeeder(UserAccountRepository users, PasswordEncoder encoder,
            @Value("${TECHLAB_ADMIN_USER:admin}") String adminName,
            @Value("${TECHLAB_ADMIN_PASSWORD}") String adminPassword,
            @Value("${TECHLAB_STUDENT_USER:estudiante}") String studentName,
            @Value("${TECHLAB_STUDENT_PASSWORD}") String studentPassword) {
        this.users = users;
        this.encoder = encoder;
        this.adminName = adminName;
        this.adminPassword = adminPassword;
        this.studentName = studentName;
        this.studentPassword = studentPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validate(adminName, adminPassword);
        validate(studentName, studentPassword);
        if (adminName.equalsIgnoreCase(studentName)) {
            throw new IllegalArgumentException("Usuarios deben ser distintos");
        }
        createIfMissing(adminName, adminPassword,
                Set.of("ROLE_USER", "ROLE_ADMIN"));
        createIfMissing(studentName, studentPassword, Set.of("ROLE_USER"));
    }

    private void createIfMissing(String name, String password,
            Set<String> roles) {
        if (!users.existsByUsername(name)) {
            users.save(new UserAccount(name, encoder.encode(password),
                    true, roles));
        }
    }

    private void validate(String name, String password) {
        if (!name.matches("[A-Za-z0-9._-]{3,80}")) {
            throw new IllegalArgumentException("Nombre de usuario inválido");
        }
        int bytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (bytes < 12 || bytes > 72) {
            throw new IllegalArgumentException(
                    "La contraseña de laboratorio debe tener 12 a 72 bytes");
        }
    }
}