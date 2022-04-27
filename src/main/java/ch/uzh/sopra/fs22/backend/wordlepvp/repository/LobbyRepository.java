package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class LobbyRepository {

    private final ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate;

    public LobbyRepository(ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    // TODO: Check for lobby name duplicates ? maybe. or maybe not. or no, not at all.
    // TODO: handle 2 browser sessions, change status on full & allow entry if not full

    public Mono<Lobby> saveLobby(Lobby lobby) {
        return this.reactiveRedisTemplate.<String, Lobby>opsForHash()
                .put("lobbies", lobby.getId(), lobby)
                .map(l -> lobby)
                .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobby/" + l.getId(), l).subscribe())
                .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobbies", l).subscribe())
                .log();

    }

    public void deleteLobby(String id) {
        this.reactiveRedisTemplate.<String, Lobby>opsForHash()
                .remove("lobbies", id)
                .subscribe();

    }

    public Mono<Lobby> getLobby(String id) {
        return this.reactiveRedisTemplate.<String, Lobby>opsForHash()
                .get("lobbies", id)
                .log();

    }

    public Flux<Lobby> getLobbyStream(String id) {
        return this.reactiveRedisTemplate.listenToChannel("lobby/" + id)
                .map(ReactiveSubscription.Message::getMessage)
                .publishOn(Schedulers.boundedElastic())
                .log();

    }

    public Flux<Lobby> getAllLobbies() {
        return this.reactiveRedisTemplate.<String, Lobby>opsForHash().values("lobbies");
    }
}