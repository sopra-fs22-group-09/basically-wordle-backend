package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.LobbyService;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.PlayerService;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import ch.uzh.sopra.fs22.backend.wordlepvp.util.AuthorizationHelper;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.GameSettingsInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInviteInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Validated
@Controller
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyService lobbyService;
    private final UserService userService;
    private final PlayerService playerService;

    @QueryMapping
    public Flux<Lobby> getLobbies() {
        return this.lobbyService.getLobbies();
    }

    @MutationMapping
    public Mono<Lobby> createLobby(@Argument @Valid LobbyInput input, @ContextValue(name = "Authorization") String authHeader) {
        User user = this.userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        Mono<Player> player = this.playerService.createPlayer(user, null);
        return this.lobbyService.initializeLobby(input, player);
    }

    @MutationMapping
    public Mono<Lobby> joinLobbyById(@Argument @Valid String id, @ContextValue(name = "Authorization") String authHeader) {
        User user = this.userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        // TODO: In lobby?
        var player = this.playerService.createPlayer(user, id);
        var lobby = this.lobbyService.getLobbyById(id);

        return player.publishOn(Schedulers.boundedElastic())
                .doFirst(() -> this.userService.setUserStatus(user.getId(), UserStatus.CREATING_LOBBY))
                .zipWith(lobby)
                .filter(pl -> !pl.getT2().getOwner().getId().equals(pl.getT1().getId()))
                .map(Tuple2::getT1)
                .doOnNext(p -> this.userService.setUserStatus(user.getId(), UserStatus.INGAME))
                .switchIfEmpty(player)
                .zipWith(this.lobbyService.addPlayerToLobby(id, player), (p, l) -> l);
    }

    @MutationMapping
    public Mono<Lobby> updateLobbySettings(@Argument @Valid GameSettingsInput input, @ContextValue(name = "Authorization") String authHeader) {
        Mono<Player> player = this.playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.lobbyService.changeLobby(input, player);
    }

    @MutationMapping
    public Mono<Boolean> inviteToLobby(@Argument @Valid LobbyInviteInput input, @ContextValue(name = "Authorization") String authHeader) {
        // TODO: Very reactive, I know...
        User sender = this.userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        UUID friendUuid;
        try {
            friendUuid = UUID.fromString(input.getRecipientId());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UUID.");
        }
        Optional<User> recipient = this.userService.friends(UserStatus.ONLINE, sender).stream().filter(f -> f.getId().equals(friendUuid)).findFirst();
        if (recipient.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only friends can be invited.");
        return this.lobbyService.sendLobbyInvite(input.getLobbyId(), recipient.get(), sender);
    }

    @SubscriptionMapping
    public Flux<Lobby> lobby(@Argument @Valid String id, @ContextValue("Authorization") String authHeader) {
        String token = AuthorizationHelper.extractAuthToken(authHeader);
        Mono<Player> player = this.playerService.getFromToken(token);
        return this.lobbyService.subscribeLobby(id, player)
                .publishOn(Schedulers.boundedElastic())
                .doFinally(ignored -> this.userService.setUserStatus(this.userService.getFromToken(token).getId(), UserStatus.ONLINE));
    }

    @SubscriptionMapping
    public Flux<LobbyInvite> lobbyInvites(@ContextValue(name = "Authorization") String authHeader) {
        return this.lobbyService.receiveLobbyInvites(this.userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader)));
    }
}