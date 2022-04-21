package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
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

    private final LobbyRepository lobbyRepository;
    private final UserService userService;

    @MutationMapping
    public Mono<Lobby> createLobby(@Argument @Valid LobbyInput input, @ContextValue(name = "Authorization") String authHeader) {
        User player = userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.lobbyRepository.saveLobby(input, player);
    }

    @MutationMapping
    public Mono<Lobby> joinLobbyById(@Argument @Valid String id, @ContextValue(name = "Authorization") String authHeader) {
        User player = userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.lobbyRepository.playerJoinLobby(id, player);
    }

/*    @MutationMapping
    public Mono<Lobby> leaveLobby(@ContextValue(name = "Authorization") String authHeader) {
        User player = userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.lobbyRepository.playerLeaveLobby(player);
    }*/

    @MutationMapping
    public Mono<Lobby> updateLobbySettings(@Argument @Valid String id, @Argument @Valid GameSettingsInput gameSettings, @ContextValue(name = "Authorization") String authHeader) {
        User player = userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.lobbyRepository.changeLobby(id, gameSettings);
    }

    @SubscriptionMapping
    public Flux<Lobby> lobby(@Argument @Valid String id, @ContextValue("Authorization") String authHeader) {
        User player = userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.lobbyRepository.getLobbyStream(id, player);
    }

    @QueryMapping
    public Flux<Lobby> getLobbies() {
        return this.lobbyRepository.getAllLobbies();
    }
}