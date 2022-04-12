package ch.uzh.sopra.fs22.backend.wordlepvp.redis;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.UUID;

// TODO: This serves purely as an example and we should eventually have one repository per entity.
@Repository
@RequiredArgsConstructor
@EnableRedisRepositories
public class DataRepository {

    private final RedisTemplate<Long, Lobby> redisTemplate;
    private final ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate;

    public Lobby getLobbyById(Long id) {
        return redisTemplate.opsForValue().get(id);
    }

    public Mono<Lobby> saveLobby(LobbyInput input) {
        Lobby lobby = Lobby.builder()
                .id(UUID.randomUUID().toString())
                .name(input.getName())
                .build();

        return reactiveRedisTemplate.opsForHash().put("lobbies", lobby.getId(), lobby)
                .map(l -> lobby)
                .log()
                .publishOn(Schedulers.boundedElastic())
                // TODO: Send key only and then read from redis in subscriber.
                .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobbysettings", lobby).subscribe());
    }

    public Flux<Lobby> getLobbyStream() {
//        return reactiveRedisTemplate.<String, Lobby>opsForHash().get("lobbies", 13L).flux();
        return this.reactiveRedisTemplate
                // TODO: Replace with pattern syntax 'lobby*'
                .listenToChannel("lobbyplayers", "lobbysettings", "lobbychat")
                .doOnNext(s -> {
                    // nothing?
                })
                .log()
                .map(ReactiveSubscription.Message::getMessage);
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
