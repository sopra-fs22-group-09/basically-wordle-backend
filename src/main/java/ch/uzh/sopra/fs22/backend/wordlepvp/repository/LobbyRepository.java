package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import org.hibernate.mapping.Collection;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class LobbyRepository {

    ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate;

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

    public Mono<Lobby> playerJoinLobby(String id) { // TODO Authorization

        return reactiveRedisTemplate.<String, Lobby>opsForHash().get("lobbies", id)
                .mapNotNull(l -> {
                    User test = User.builder().id(UUID.randomUUID()).username("test player 2").build();
                    List<User> users = new ArrayList<>(l.getPlayers());
                    users.add(test);
                    l.setPlayers(new HashSet<>(users));
                    return l;
                })
                .doOnNext(l -> this.reactiveRedisTemplate.<String, Lobby>opsForHash().put("lobbies", l.getId(), l))
                .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobbyplayers", l).subscribe());

        //return reactiveRedisTemplate.<String, Lobby>opsForHash().get("lobbies", id);
    }

    public Flux<Lobby> getLobbyStream() { // TODO Authorization
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
