package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.PlayerRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.LobbyService;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Validated
@Controller
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerRepository playerRepository;

    private final PlayerService playerService;

    @MutationMapping
    public Mono<Player> createGuest(@Argument @Valid String id) {
        return this.playerService.createPlayer(id);
    }

}
