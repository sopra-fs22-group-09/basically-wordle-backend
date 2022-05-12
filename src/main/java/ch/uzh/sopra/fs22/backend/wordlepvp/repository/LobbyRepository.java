package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class LobbyRepository {

    private final ReactiveRedisTemplate<String, Lobby> reactiveLobbyRedisTemplate;

    private final ReactiveRedisTemplate<String, LobbyInvite> reactiveInviteRedisTemplate;

    public LobbyRepository(ReactiveRedisTemplate<String, Lobby> reactiveLobbyRedisTemplate, ReactiveRedisTemplate<String, LobbyInvite> reactiveInviteRedisTemplate) {
        this.reactiveLobbyRedisTemplate = reactiveLobbyRedisTemplate;
        this.reactiveInviteRedisTemplate = reactiveInviteRedisTemplate;
    }

    // TODO: Check for lobby name duplicates ? maybe. or maybe not. or no, not at all.
    // TODO: handle 2 browser sessions, change status on full & allow entry if not full

    public Mono<Lobby> saveLobby(Lobby lobby) {
        return this.reactiveLobbyRedisTemplate.<String, Lobby>opsForHash()
                .put("lobbies", lobby.getId(), lobby)
                .map(l -> lobby)
                .flatMap(l -> this.reactiveLobbyRedisTemplate.convertAndSend("lobby/" + l.getId(), l).thenReturn(l))
                .flatMap(l -> this.reactiveLobbyRedisTemplate.convertAndSend("lobbies", l).thenReturn(l))
                .log();

    }

    public Mono<Long> deleteLobby(String id) {
        return this.reactiveLobbyRedisTemplate.<String, Lobby>opsForHash()
                .remove("lobbies", id)
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

    public Flux<LobbyInvite> getInvitesStream(User user) {
        return this.reactiveInviteRedisTemplate.listenToChannel("invite/" + user.getId().toString())
                .map(ReactiveSubscription.Message::getMessage);

    }

    public Mono<Boolean> inviteToLobby(LobbyInvite invite) {
        return this.reactiveInviteRedisTemplate.<String, LobbyInvite>opsForHash()
                .put("invites", invite.getId(), invite)
                .map(i -> invite)
                .flatMap(i -> this.reactiveInviteRedisTemplate.convertAndSend("invite/" + i.getRecipientId(), i))
                .thenReturn(true)
                .log();

    }
}