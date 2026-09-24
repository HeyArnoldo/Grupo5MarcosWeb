package pe.edu.utp.techlab.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public final class StartupReporter implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupReporter.class);
    private final Environment environment;

    public StartupReporter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String name = environment.getProperty("spring.application.name", "application");
        String port = environment.getProperty("server.port", "8080");
        LOGGER.info("{} disponible en http://localhost:{}", name, port);
    }
}
