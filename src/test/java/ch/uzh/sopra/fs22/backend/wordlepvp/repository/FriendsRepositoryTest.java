package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.RedisConfig;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@DataRedisTest
@ActiveProfiles("test")
@Import({RedisConfig.class, FriendsRepository.class})
@Testcontainers
public class FriendsRepositoryTest {

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
    FriendsRepository friendsRepository;

    @Autowired
    ReactiveRedisOperations<String, User> reactiveRedisOperations;

    @Test
    void testContainersRunning() {
        assertTrue(redisContainer.isRunning());
    }

    @Test
    public void getFriendsStreamTest() {

        UUID testUserId = UUID.randomUUID();
        UUID testFriendId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();
        User testFriend = User.builder()
                .id(testFriendId)
                .build();

        this.friendsRepository.broadcastFriendsEvent(testFriend).subscribe();
        Mono<User> friend = this.reactiveRedisOperations
                .convertAndSend("friends", testFriend).thenReturn(testFriend);

        this.friendsRepository.getFriendsStream(testUser)
                .as(lobbyFlux -> StepVerifier.create(friend))
                .expectNext(testFriend)
                .thenAwait()
                .thenCancel()
                .verify();
    }
}
