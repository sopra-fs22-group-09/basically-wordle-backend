package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.RedisConfig;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@DataRedisTest
@ActiveProfiles("test")
@Import({RedisConfig.class, AuthRepository.class})
@Testcontainers
public class AuthRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(AuthRepositoryTest.class);

    private static final GenericContainer<?> redisContainer;

    static {
        redisContainer = new GenericContainer<>("redis:alpine").withExposedPorts(6379)
                .withLogConsumer(new Slf4jLogConsumer(log));
        redisContainer.start();
    }

    @DynamicPropertySource
    public static void setDatasourceProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }

    @Autowired
    AuthRepository authRepository;

    @Autowired
    RedisOperations<String, UUID> redisOperations;

    @AfterEach
    void breakdown() {
        this.redisOperations.opsForValue();
    }

    @Test
    public void setAuthTokenTest() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();
        String authToken = this.authRepository.setAuthToken(testUser);

        UUID userId = this.redisOperations.opsForValue().get(authToken);

        assertEquals(testUserId, userId);
    }

    @Test
    public void getUserIdTest() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();
        String authToken = this.authRepository.setAuthToken(testUser);

        UUID userId = this.authRepository.getUserID(authToken);

        assertEquals(testUserId, userId);
    }

    @Test
    public void expireTest() {
        assertDoesNotThrow(() -> {

            UUID testUserId = UUID.randomUUID();
            User testUser = User.builder()
                    .id(testUserId)
                    .build();
            String authToken = this.authRepository.setAuthToken(testUser);

            this.authRepository.expire(authToken);
        });
    }

    @Test
    public void isAuthorizedTest() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();
        String authToken = this.authRepository.setAuthToken(testUser);

        Boolean authed = this.authRepository.isAuthorized(authToken);

        assertEquals(true, authed);
    }

    @Test
    public void isPretendUserTest() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();
        String authToken = this.authRepository.setAuthToken(testUser);

        Boolean pretend = this.authRepository.isPretendUser(testUser, authToken);

        assertEquals(true, pretend);
    }
}