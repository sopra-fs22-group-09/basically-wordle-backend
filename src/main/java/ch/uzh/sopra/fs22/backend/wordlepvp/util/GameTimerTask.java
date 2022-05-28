package ch.uzh.sopra.fs22.backend.wordlepvp.util;

import ch.uzh.sopra.fs22.backend.wordlepvp.logic.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStatus;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.GameService;
import reactor.core.scheduler.Schedulers;

import java.util.TimerTask;

public class GameTimerTask extends TimerTask {

    private final String gameId;
    private final GameRepository gameRepository;
    private final GameService gameService;

    public GameTimerTask(String game, GameRepository gameRepository, GameService gameService) {
        this.gameId = game;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
    }

    @Override
    public void run() {
        this.gameRepository.getGame(gameId)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(Game::endRound)
                .doOnNext(this.gameService::restartTimer)
                .flatMap(this.gameRepository::saveGame)
                .subscribe();
    }
}
