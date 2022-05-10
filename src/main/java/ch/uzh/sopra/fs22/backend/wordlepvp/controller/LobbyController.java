package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.LobbyService;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.PlayerService;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import ch.uzh.sopra.fs22.backend.wordlepvp.util.AuthorizationHelper;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.GameSettingsInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Validated
@Controller
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyService lobbyService;
    private final UserService userService;
    private final PlayerService playerService;

    @MutationMapping
    public Mono<Lobby> createLobby(@Argument @Valid LobbyInput input, @ContextValue(name = "Authorization") String authHeader) {
        User user = this.userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        Mono<Player> player = this.playerService.createPlayer(user, null);
        return this.lobbyService.initializeLobby(input, player);
    }

    @MutationMapping
    public Mono<Lobby> joinLobbyById(@Argument @Valid String id, @ContextValue(name = "Authorization") String authHeader) {
        User user = this.userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        Mono<Player> player = this.playerService.createPlayer(user, id);
        return this.lobbyService.addPlayerToLobby(id, player);
    }

    @MutationMapping
    public Mono<Lobby> guestJoinLobbyById(@Argument @Valid String id, @ContextValue(name = "Authorization") String authHeader) {
        Mono<Player> player = this.playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.lobbyService.addPlayerToLobby(id, player);
    }

    @MutationMapping
    public Mono<Lobby> updateLobbySettings(@Argument @Valid GameSettingsInput input, @ContextValue(name = "Authorization") String authHeader) {
        Mono<Player> player = this.playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.lobbyService.changeLobby(input, player);
    }

    @SubscriptionMapping
    public Flux<Lobby> lobby(@ContextValue("Authorization") String authHeader) {
        Mono<Player> player = this.playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.lobbyService.subscribeLobby(player);
    }

    @QueryMapping
    public Flux<Lobby> getLobbies() {
        return this.lobbyService.getLobbies();
    }
}