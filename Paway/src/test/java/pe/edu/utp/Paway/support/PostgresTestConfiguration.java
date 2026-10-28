package pe.edu.utp.Paway.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Real PostgreSQL for every Spring test context. {@link ServiceConnection} wires the
 * datasource to the container, so Flyway migrates and Hibernate validates the same
 * schema used in production. Spring caches the context, so the container is shared.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestConfiguration {
    public static final String IMAGE = "postgres:16-alpine";

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer(IMAGE);
    }
}
