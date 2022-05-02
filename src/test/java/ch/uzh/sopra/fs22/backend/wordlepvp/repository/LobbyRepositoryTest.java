package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.RedisConfig;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameCategory;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.LobbyStatus;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@DataRedisTest
@ActiveProfiles("test")
@Import({RedisConfig.class, LobbyRepository.class})
@Testcontainers
public class LobbyRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(LobbyRepositoryTest.class);

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
    LobbyRepository lobbyRepository;

    @Autowired
    ReactiveRedisOperations<String, Lobby> reactiveRedisOperations;

    @Test
    void testContainersRunning() {
        assertTrue(redisContainer.isRunning());
    }

    @Test
    public void saveLobbyTest() {

        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        this.lobbyRepository.saveLobby(testLobby).subscribe();

        Mono<Lobby> lobby = this.reactiveRedisOperations.<String, Lobby>opsForHash()
                .get("lobbies", "deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void deleteLobbyTest() {

        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        this.reactiveRedisOperations.<String, Lobby>opsForHash()
                .put("lobbies", testLobby.getId(), testLobby);

        Mono<Long> deleted = this.lobbyRepository.deleteLobby("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(deleted)
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    public void getLobbyTest() {

        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        this.reactiveRedisOperations.<String, Lobby>opsForHash()
                .put("lobbies", testLobby.getId(), testLobby).subscribe();

        Mono<Lobby> lobby = this.lobbyRepository.getLobby("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

}
