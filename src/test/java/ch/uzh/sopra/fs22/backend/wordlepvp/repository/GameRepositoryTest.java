package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.RedisConfig;
import ch.uzh.sopra.fs22.backend.wordlepvp.logic.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes.SonicFast;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes.WordsPP;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@DataRedisTest
@ActiveProfiles("test")
@Import({RedisConfig.class, GameRepository.class})
@Testcontainers
public class GameRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(GameRepositoryTest.class);

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
    GameRepository gameRepository;

    @Autowired
    ReactiveRedisOperations<String, Game> reactiveGameRedisOperations;

    @Autowired
    ReactiveRedisOperations<String, GameStatus> reactiveGameStatusRedisOperations;

    @AfterEach
    void breakdown() {
        this.reactiveGameRedisOperations.opsForHash().delete("games").subscribe();
    }

    @Test
    void testContainersRunning() {
        assertTrue(redisContainer.isRunning());
    }

    @Test
    public void saveGameTest() {

        Game testGame = new WordsPP();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");
        testGame.setAmountRounds(3);
        testGame.setRoundTime(150);
        this.gameRepository.saveGame(testGame).subscribe();

        Mono<Game> game = this.reactiveGameRedisOperations.<String, Game>opsForHash()
                .get("games", "deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(game)
                .expectNext(testGame)
                .verifyComplete();
    }

    @Test
    public void deleteGameTest() {

        Game testGame = new WordsPP();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");
        testGame.setAmountRounds(3);
        testGame.setRoundTime(150);
        this.reactiveGameRedisOperations.<String, Game>opsForHash()
                .put("games", testGame.getId(), testGame)
                .subscribe();

        Mono<Long> deleted = this.gameRepository.deleteGame("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(deleted)
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    public void getGameTest() {

        Game testGame = new WordsPP();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");
        testGame.setAmountRounds(3);
        testGame.setRoundTime(150);
        this.reactiveGameRedisOperations.<String, Game>opsForHash()
                .put("games", testGame.getId(), testGame).subscribe();

        Mono<Game> game = this.gameRepository.getGame("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(game)
                .expectNext(testGame)
                .verifyComplete();
    }

    @Test
    public void getGameStatusStreamTest() {

        Player testPlayer = Player.builder().build();
        Game testGame = new SonicFast();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");

        Mono<GameStatus> gameStatus = this.reactiveGameStatusRedisOperations
                .convertAndSend("gameSync/game/" + testGame.getId(), GameStatus.NEW)
                .thenReturn(GameStatus.NEW);

        this.gameRepository.getGameStatusStream("deadbeef-dead-beef-caff-deadbeefcaff", testPlayer)
                .as(lobbyFlux -> StepVerifier.create(gameStatus))
                .expectNext(GameStatus.NEW)
                .thenAwait()
                .thenCancel()
                .verify();
    }

    @Test
    public void getGameStreamTest() {

        Game testGame = new SonicFast();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");

        Mono<Game> game = this.reactiveGameRedisOperations
                .convertAndSend("game/" + testGame.getId(), testGame)
                .thenReturn(testGame);

        this.gameRepository.getGameStream("deadbeef-dead-beef-caff-deadbeefcaff")
                .as(lobbyFlux -> StepVerifier.create(game))
                .expectNext(testGame)
                .thenAwait()
                .thenCancel()
                .verify();
    }

    @Test
    public void broadcastGameStatusSingleTest() {

        Player testPlayer = Player.builder()
                .id("testId").build();

        Mono<Long> broadCasted = this.gameRepository.broadcastGameStatusSingle(testPlayer.getId(), GameStatus.NEW);

        StepVerifier.create(broadCasted)
                .expectNext(0L)
                .verifyComplete();

    }

    @Test
    public void broadcastGameStatusTest() {

        Game testGame = new SonicFast();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");

        Mono<Long> broadCasted = this.gameRepository.broadcastGameStatus(testGame, GameStatus.NEW);

        StepVerifier.create(broadCasted)
                .expectNext(0L)
                .verifyComplete();

    }
}