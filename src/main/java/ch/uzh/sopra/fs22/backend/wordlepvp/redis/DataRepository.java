package ch.uzh.sopra.fs22.backend.wordlepvp.redis;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

// TODO: This serves purely as an example and we should eventually have one repository per entity.
@Repository
@RequiredArgsConstructor
@EnableRedisRepositories
public class DataRepository {

    private final RedisTemplate<Long, Lobby> redisTemplate;

    public Lobby saveLobby(LobbyInput input) {
        Lobby lobby = Lobby.builder()
                .id(input.getId())
                .name(input.getName())
                .build();

        redisTemplate.opsForValue().set(lobby.getId(), lobby);
        return redisTemplate.opsForValue().get(lobby.getId());
    }

    public Lobby getLobbyById(Long id) {
        return redisTemplate.opsForValue().get(id);
    }

    public Flux<String> getGreetings() {
        return Mono.delay(Duration.ofMillis(500))
                .flatMapMany(aLong -> Flux.just("Hi!", "Bonjour!", "Hola!", "Ciao!", "Zdravo!"));
    }

    public Flux<String> getGreetingsStream() {
        return Mono.delay(Duration.ofMillis(2000))
                .flatMapMany(aLong -> Flux
                .just("Hi!", "Bonjour!", "Hola!", "Ciao!", "Zdravo!")
                .log()
                .delayElements(Duration.ofSeconds(2)));
    }
}
