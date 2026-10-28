package pe.edu.utp.Paway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import pe.edu.utp.Paway.support.PostgresTestConfiguration;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class PawayApplicationTests {

	@Test
	void contextLoads() {
	}

}
