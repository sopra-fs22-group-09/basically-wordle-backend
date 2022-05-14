package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

@Component
public class LobbyRepository {

    private final ReactiveRedisTemplate<String, Lobby> reactiveLobbyRedisTemplate;
    private final ReactiveRedisTemplate<String, List<Lobby>> reactiveLobbiesRedisTemplate;

    public LobbyRepository(ReactiveRedisTemplate<String, Lobby> reactiveLobbyRedisTemplate, ReactiveRedisTemplate<String, List<Lobby>> reactiveLobbiesRedisTemplate) {
        this.reactiveLobbyRedisTemplate = reactiveLobbyRedisTemplate;
        this.reactiveLobbiesRedisTemplate = reactiveLobbiesRedisTemplate;
    }

    public Mono<Lobby> saveLobby(Lobby lobby) {
        return this.reactiveLobbyRedisTemplate.<String, Lobby>opsForHash()
                .put("lobbies", lobby.getId(), lobby)
                .map(l -> lobby)
                .flatMap(l -> this.reactiveLobbyRedisTemplate.convertAndSend("lobby/" + l.getId(), l).thenReturn(l))
                .zipWith(getAllLobbies().collectList())
                .flatMap(ll -> this.reactiveLobbiesRedisTemplate.convertAndSend("lobbies", ll.getT2()).thenReturn(ll))
                .map(Tuple2::getT1)
                .log();

    }

    public Mono<Long> deleteLobby(String id) {
        return this.reactiveLobbyRedisTemplate.<String, Lobby>opsForHash()
                .remove("lobbies", id)
                .zipWith(getAllLobbies().collectList())
                .flatMap(ll -> this.reactiveLobbiesRedisTemplate.convertAndSend("lobbies", ll.getT2()).thenReturn(ll))
                .map(Tuple2::getT1)
                .log();

    }

    public Mono<Lobby> getLobby(String id) {
        return this.reactiveLobbyRedisTemplate.<String, Lobby>opsForHash()
                .get("lobbies", id)
                .log();

    }

    public Flux<Lobby> getLobbyStream(String id) {
        return this.reactiveLobbyRedisTemplate.listenToChannel("lobby/" + id)
                .map(ReactiveSubscription.Message::getMessage)
                .log();

    }

    public Flux<Lobby> getAllLobbies() {
        return this.reactiveLobbyRedisTemplate.<String, Lobby>opsForHash().values("lobbies");

    }

    public Flux<List<Lobby>> getAllLobbiesStream() {
        return this.reactiveLobbiesRedisTemplate.listenToChannel("lobbies")
                .map(ReactiveSubscription.Message::getMessage)
                .log();

    }
}