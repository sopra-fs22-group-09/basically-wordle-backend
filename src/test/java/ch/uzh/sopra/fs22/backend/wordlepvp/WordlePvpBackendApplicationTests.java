package ch.uzh.sopra.fs22.backend.wordlepvp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class WordlePvpBackendApplicationTests {

	private static final PostgreSQLContainer postgresqlContainer;

	static {
		postgresqlContainer = (PostgreSQLContainer)(new PostgreSQLContainer("postgres:13-alpine")
				.withUsername("testcontainers")
				.withPassword("Testcontain3rs!")
				.withReuse(true));
		postgresqlContainer.start();
	}

	@DynamicPropertySource
	public static void setDatasourceProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
		registry.add("spring.datasource.password", postgresqlContainer::getPassword);
		registry.add("spring.datasource.username", postgresqlContainer::getUsername);
	}

	private static final GenericContainer redisContainer;
	static {
		redisContainer = new GenericContainer("redis:alpine").withExposedPorts(6379);
		redisContainer.start();
	}

	@Test
	void testContainersRunning() {
		assertTrue(postgresqlContainer.isRunning());
		assertTrue(redisContainer.isRunning());
	}

	@Test
	void contextLoads() {
	}

}
