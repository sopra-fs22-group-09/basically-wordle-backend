package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.RedisConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@DataRedisTest
@ActiveProfiles("test")
@Import({RedisConfig.class, WordsRepository.class})
@Testcontainers
public class WordsRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(WordsRepositoryTest.class);

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
    WordsRepository wordsRepository;

    @Autowired
    ReactiveRedisOperations<String, String> reactiveRedisOperations;

    @AfterEach
    void breakdown() {
        this.reactiveRedisOperations.opsForHash().delete("allWords").subscribe();
    }

    @Test
    void testContainersRunning() {
        assertTrue(redisContainer.isRunning());
    }

    @Test
    public void getRandomWordsTest() {

        String[] words = this.wordsRepository.getRandomWords(250);

        assertEquals(250, words.length);
    }

    @Test
    public void getWordsByTopicsTest() {

        String[] topics = {"dogs", "cats"};
        String[] words = this.wordsRepository.getWordsByTopics(topics, 50);

        assertEquals(5, words[0].length());
        assertEquals(50, words.length);
    }

    @Test
    public void getWordsByTopicsTestTooMany() {

        String[] topics = {"dogs", "cats"};

        assertThrows(ResponseStatusException.class, () -> this.wordsRepository.getWordsByTopics(topics, 5000));
    }

}