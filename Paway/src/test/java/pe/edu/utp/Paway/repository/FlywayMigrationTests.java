package pe.edu.utp.Paway.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import pe.edu.utp.Paway.support.PostgresTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs the real migrations on a brand-new, empty PostgreSQL (no Spring involved). */
@Testcontainers
class FlywayMigrationTests {
    @Container
    static final PostgreSQLContainer EMPTY = new PostgreSQLContainer(PostgresTestConfiguration.IMAGE);

    @Test
    void migratesAnEmptyDatabaseAndValidatesTheHistory() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(EMPTY.getJdbcUrl(), EMPTY.getUsername(), EMPTY.getPassword())
                .locations("classpath:db/migration").load();

        MigrateResult result = flyway.migrate();
        assertThat(result.success).isTrue();
        assertThat(result.migrations).extracting(m -> m.version).containsExactly("1", "2");
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.migrate().migrationsExecuted).isZero();

        try (Connection connection = DriverManager.getConnection(EMPTY.getJdbcUrl(), EMPTY.getUsername(),
                EMPTY.getPassword()); ResultSet rows = connection.createStatement()
                        .executeQuery("select guide from shipment order by id")) {
            List<String> guides = new ArrayList<>();
            while (rows.next()) guides.add(rows.getString(1));
            String year = String.valueOf(Year.now(ZoneId.of("America/Lima")).getValue());
            assertThat(guides).containsExactly("PW-" + year + "-0001", "PW-" + year + "-0002",
                    "PW-" + year + "-0003", "PW-" + year + "-0004");
        }
    }
}
