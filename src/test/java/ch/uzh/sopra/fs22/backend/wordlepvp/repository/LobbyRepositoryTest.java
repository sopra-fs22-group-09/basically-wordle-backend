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
import static reactor.core.publisher.Mono.when;

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
    ReactiveRedisOperations<String, Lobby> reactiveLobbyRedisOperations;

    @Autowired
    ReactiveRedisOperations<String, LobbyInvite> reactiveLobbyInviteRedisOperations;

    @AfterEach
    void breakdown() {
        this.reactiveLobbyRedisOperations.opsForHash().delete("lobbies").subscribe();
        this.reactiveLobbyInviteRedisOperations.opsForHash().delete("invites").subscribe();
    }

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

        Mono<Lobby> lobby = this.reactiveLobbyRedisOperations.<String, Lobby>opsForHash()
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
        this.reactiveLobbyRedisOperations.<String, Lobby>opsForHash()
                .put("lobbies", testLobby.getId(), testLobby)
                .subscribe();

        Mono<Long> deleted = this.lobbyRepository.deleteLobby("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(deleted)
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    public void hasLobbyTest() {

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
        this.reactiveLobbyRedisOperations.<String, Lobby>opsForHash()
                .put("lobbies", testLobby.getId(), testLobby).subscribe();

        Mono<Boolean> lobbyExistence = this.lobbyRepository.hasLobby("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(lobbyExistence)
                .expectNext(true)
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
        this.reactiveLobbyRedisOperations.<String, Lobby>opsForHash()
                .put("lobbies", testLobby.getId(), testLobby).subscribe();

        Mono<Lobby> lobby = this.lobbyRepository.getLobby("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void getLobbyStreamTest() {

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

        Mono<Lobby> lobby = this.reactiveLobbyRedisOperations.<String, Lobby>opsForHash()
                .put("lobbies", testLobby.getId(), testLobby)
                .map(l -> testLobby)
                .flatMap(l -> this.reactiveLobbyRedisOperations.convertAndSend("lobby/" + l.getId(), l).thenReturn(testLobby));

        this.lobbyRepository.getLobbyStream("deadbeef-dead-beef-caff-deadbeefcaff")
                .as(lobbyFlux -> StepVerifier.create(lobby))
                .expectNext(testLobby)
                .thenAwait()
                .thenCancel()
                .verify();
    }

    @Test
    public void getAllLobbiesTest() {

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
        this.reactiveLobbyRedisOperations.<String, Lobby>opsForHash()
                .put("lobbies", testLobby.getId(), testLobby).subscribe();

        Flux<Lobby> lobby = this.lobbyRepository.getAllLobbies();

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void getAllLobbiesStreamTest() {

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

        Mono<Lobby> lobby = this.reactiveLobbyRedisOperations.<String, Lobby>opsForHash()
                .put("lobbies", testLobby.getId(), testLobby)
                .map(l -> testLobby)
                .flatMap(l -> this.reactiveLobbyRedisOperations.convertAndSend("lobbies" + l.getId(), l).thenReturn(testLobby));

        this.lobbyRepository.getAllLobbiesStream()
                .as(lobbyFlux -> StepVerifier.create(lobby))
                .expectNext(testLobby)
                .thenAwait()
                .thenCancel()
                .verify();
    }

    @Test
    public void getInvitesStreamTest() {

        UUID testRecipientId = UUID.randomUUID();
        LobbyInvite testLobbyInvite = LobbyInvite.builder()
                .id(UUID.randomUUID().toString())
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .senderId("sender")
                .recipientId(testRecipientId.toString())
                .build();
        User testRecipient = User.builder()
                .id(testRecipientId)
                .build();

        Mono<LobbyInvite> lobbyInvite = this.reactiveLobbyInviteRedisOperations.<String, LobbyInvite>opsForHash()
                .put("invites", testLobbyInvite.getId(), testLobbyInvite)
                .map(i -> testLobbyInvite)
                .flatMap(i -> this.reactiveLobbyInviteRedisOperations.convertAndSend("invite/" + i.getRecipientId(), i).thenReturn(testLobbyInvite));

        this.lobbyRepository.getInvitesStream(testRecipient)
                .as(lobbyInviteFlux -> StepVerifier.create(lobbyInvite))
                .expectNext(testLobbyInvite)
                .thenAwait()
                .thenCancel()
                .verify();
    }

    @Test
    public void inviteToLobbyTest() {

        LobbyInvite lobbyInvite = LobbyInvite.builder()
                .id(UUID.randomUUID().toString())
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .senderId("sender")
                .recipientId("recipient")
                .build();
        Mono<Boolean> invite = this.lobbyRepository.inviteToLobby(lobbyInvite);

        StepVerifier.create(invite)
                .expectNext(true)
                .verifyComplete();
    }
}
