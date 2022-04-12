package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Component
public class LobbyRepository {

    ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate;

    public LobbyRepository(ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Lobby> getLobbyById(String id) {
        return reactiveRedisTemplate.<String, Lobby>opsForHash().get("lobbies", id);
    }

    public Mono<Lobby> saveLobby(LobbyInput input) {
        Lobby lobby = Lobby.builder()
                .id(UUID.randomUUID().toString())
                .gameCategory(input.getGameCategory())
                .name(input.getName())
                .size(input.getSize())
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
}
