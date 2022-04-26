package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.UserRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.WordsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;

@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final WordsRepository wordsRepository;

    @Autowired
    public GameService(GameRepository gameRepository, WordsRepository wordsRepository) {
        this.gameRepository = gameRepository;
        this.wordsRepository = wordsRepository;
    }

    public Game createGame(GameMode gameMode) {
        try {
            Class<? extends Game> gameClass = Class.forName("ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes." + gameMode.getClassName()).asSubclass(Game.class);
            return gameClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find Game.");
        }
    }

    public Mono<Game> initializeGame(Mono<Player> player) {

        return this.gameRepository.getGameByPlayer(player)
                .map(game -> {
                    game.start(wordsRepository.getRandomWords(250));
                    return game;
                })
                .flatMap(game -> this.gameRepository.updateGameByPlayer(player, game))
                .log();
    }

    public Mono<GameRound> submitWord(String word, Mono<Player> player) {

        return this.gameRepository.getGameByPlayer(player)
                .map(game -> {
                    game.guess(word);
                    return game;
                })
                .flatMap(game -> this.gameRepository.updateGameByPlayer(player, game))
                .map(Game::getGameRound)
                .log();

    }

    public Mono<GameStats> getConclusion(Mono<Player> player) {

        return this.gameRepository.getGameByPlayer(player)
                .map(Game::concludeGame)
                .log();
    }

    public Flux<GameRound[]> getOpponentRounds(Mono<Player> player) {
        return null;
    }
}