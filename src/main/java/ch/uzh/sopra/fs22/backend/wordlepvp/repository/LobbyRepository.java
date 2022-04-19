package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import org.springframework.data.redis.connection.ReactiveHashCommands;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class LobbyRepository {

    private ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate;
    private ReactiveHashCommands reactiveHashCommands;

    public LobbyRepository(ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Lobby> saveLobby(LobbyInput input) { // TODO Authorization

        Lobby lobby = Lobby.builder()
                .id(UUID.randomUUID().toString())
                .gameCategory(input.getGameCategory())
                .name(input.getName())
                .size(input.getSize())
                .players(Set.of(User.builder().id(UUID.randomUUID()).username("test player 1").build()))
                .build();

        return this.reactiveRedisTemplate.opsForHash().put("lobbies", lobby.getId(), lobby)
                .map(l -> lobby)
                .log()
                .publishOn(Schedulers.boundedElastic())
                // TODO: Send key only and then read from redis in subscriber.
                .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobbysettings", lobby).subscribe());
    }

    public Mono<Lobby> playerJoinLobby(String id, User player) {

        return reactiveRedisTemplate.<String, Lobby>opsForHash().get("lobbies", id)
                .mapNotNull(l -> {
                    List<User> users = new ArrayList<>(l.getPlayers());
                    users.add(player);
                    l.setPlayers(new HashSet<>(users));
                    return l;
                })
                .doOnNext(l -> this.reactiveRedisTemplate.<String, Lobby>opsForHash().put("lobbies", l.getId(), l).subscribe())
                .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobbyplayers", l).subscribe());
    }

    public Mono<Lobby> playerLeaveLobby(User player) {

        return this.reactiveRedisTemplate.<String, Lobby>opsForHash().values("lobbies")
                .filter(l -> l.getPlayers().contains(player))
                .mapNotNull(l -> {
                    List<User> users = new ArrayList<>(l.getPlayers());
                    users.remove(player);
                    l.setPlayers(new HashSet<>(users));
                    return l;
                })
                .single()
                .doOnNext(l -> this.reactiveRedisTemplate.<String, Lobby>opsForHash().put("lobbies", l.getId(), l).subscribe())
                .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobbyplayers", l).subscribe());
    }

    public Flux<Lobby> getLobbyStream(User player) { // TODO Authorization
//        return reactiveRedisTemplate.<String, Lobby>opsForHash().get("lobbies", 13L).flux();

        return this.reactiveRedisTemplate
                // TODO: Replace with pattern syntax 'lobby*'
                .listenToChannel("lobbyplayers", "lobbysettings", "lobbychat")
                //.filter(stringLobbyMessage -> stringLobbyMessage.getMessage().getPlayers().contains(player))
                .doOnNext(s -> {
                    // nothing?
                })
                .doOnCancel(() -> {/* remove player from lobby*/})
                .log()
                .map(ReactiveSubscription.Message::getMessage);
    }

    public Flux<Lobby> getAllLobbies() {
        return this.reactiveRedisTemplate.<String, Lobby>opsForHash().values("lobbies");
    }
}