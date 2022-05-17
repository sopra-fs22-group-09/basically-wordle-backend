package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.AuthRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@DataRedisTest
@ActiveProfiles("test")
public class PlayerServiceTest {

    @Mock
    PlayerRepository playerRepository;

    @Mock
    AuthRepository authRepository;

    @InjectMocks
    PlayerService playerService;

    @Test
    public void createPlayerTest() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .username("testUser")
                .build();
        Player testPlayer = Player.builder()
                .id(testUserId.toString())
                .build();

        when(playerRepository.findById(Mockito.anyString())).thenReturn(Mono.empty());
        when(playerRepository.savePlayer(Mockito.any())).thenReturn(Mono.just(testPlayer));

        Mono<Player> player = playerService.createPlayer(testUser);

        StepVerifier.create(player)
                .expectNext(testPlayer)
                .verifyComplete();
    }

    @Test
    public void getFromTokenTest() {

        UUID testUserId = UUID.randomUUID();
        Player testPlayer = Player.builder()
                .id(testUserId.toString())
                .build();

        when(authRepository.getUserID("testToken")).thenReturn(testUserId);
        when(playerRepository.findById(Mockito.anyString())).thenReturn(Mono.just(testPlayer));

        Mono<Player> player = playerService.getFromToken("testToken");

        StepVerifier.create(player)
                .expectNext(testPlayer)
                .verifyComplete();
    }
}
