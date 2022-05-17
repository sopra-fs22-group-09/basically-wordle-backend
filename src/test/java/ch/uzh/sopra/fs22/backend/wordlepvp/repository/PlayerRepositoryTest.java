package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.RedisConfig;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@DataRedisTest
@ActiveProfiles("test")
@Import({RedisConfig.class, PlayerRepository.class})
@Testcontainers
public class PlayerRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(PlayerRepositoryTest.class);

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
    PlayerRepository playerRepository;

    @Autowired
    ReactiveRedisOperations<String, Player> reactiveRedisOperations;

    @AfterEach
    void breakdown() {
        this.reactiveRedisOperations.opsForHash().delete("players").subscribe();
    }

    @Test
    void testContainersRunning() {
        assertTrue(redisContainer.isRunning());
    }

    @Test
    public void savePlayerTest() {

        Player testPlayer = Player.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .avatarID(null)
                .build();
        this.playerRepository.savePlayer(testPlayer).subscribe();

        Mono<Player> player = this.reactiveRedisOperations.<String, Player>opsForHash()
                .get("players", "deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(player)
                .expectNext(testPlayer)
                .verifyComplete();
    }

    @Test
    public void findByIdTest() {

        Player testPlayer = Player.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .avatarID(null)
                .build();
        this.reactiveRedisOperations.<String, Player>opsForHash()
                .put("players", testPlayer.getId(), testPlayer).subscribe();

        Mono<Player> player = this.playerRepository.findById("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(player)
                .expectNext(testPlayer)
                .verifyComplete();
        }
}