package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataRedisTest
@ActiveProfiles("test")
public class EmailServiceTest {

    @InjectMocks
    EmailService emailService;

    @Test
    public void sendSimpleMessageTest() {

        assertThrows(NullPointerException.class, () -> emailService.sendSimpleMessage("test", "test", "test"));
    }
}
