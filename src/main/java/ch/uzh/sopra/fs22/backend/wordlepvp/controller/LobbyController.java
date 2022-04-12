package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
@Controller
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyRepository lobbyRepository;

    @MutationMapping
    public Mono<Lobby> createLobby(@Argument @Valid LobbyInput input) {
        return this.lobbyRepository.saveLobby(input);
    }

    @SubscriptionMapping
    public Flux<Lobby> lobby() {
        return this.lobbyRepository.getLobbyStream();
    }
}
