package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStats;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.GameService;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.PlayerService;
import ch.uzh.sopra.fs22.backend.wordlepvp.util.AuthorizationHelper;
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
public class GameController {

    private final GameService gameService;
    private final PlayerService playerService;

    @MutationMapping
    public Mono<Game> startGame(@ContextValue(name = "Authorization") String authHeader) {
        Mono<Player> player = playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.gameService.initializeGame(player);
    }

    @MutationMapping
    public Mono<GameRound> submitGuess(@Argument @Valid String word, @ContextValue(name = "Authorization") String authHeader) {
        Mono<Player> player = playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.gameService.submitWord(word, player);
    }

    @QueryMapping
    public Mono<GameStats> concludeGame(@ContextValue(name = "Authorization") String authHeader) {
        Mono<Player> player = playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.gameService.getConclusion(player);
    }

    @SubscriptionMapping
    public Flux<GameRound[]> opponentGameRound(@ContextValue(name = "Authorization") String authHeader) {
        Mono<Player> player = playerService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.gameService.getOpponentGameRounds(player);
    }
}