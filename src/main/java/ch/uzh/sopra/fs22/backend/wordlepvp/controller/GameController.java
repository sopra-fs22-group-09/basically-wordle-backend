package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.PlayerService;
import ch.uzh.sopra.fs22.backend.wordlepvp.util.AuthorizationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Validated
@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameRepository gameRepository;
    private final PlayerService playerService;

    @MutationMapping
    public Mono<Game> startGame(@ContextValue(name = "Authorization") String authHeader) {
        Mono<Player> player = playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.gameRepository.initializeGame(player);
    }

    @MutationMapping //TODO IS IT GAMEROUND?
    public Mono<GameRound> submitGuess(@Argument @Valid String word, @ContextValue(name = "Authorization") String authHeader) {
        Mono<Player> player = playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        Mono<GameRound> game = this.gameRepository.getGame(player);

        return game;
    }
}
