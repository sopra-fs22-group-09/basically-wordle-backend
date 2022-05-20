package ch.uzh.sopra.fs22.backend.wordlepvp.util;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import reactor.core.scheduler.Schedulers;

import java.util.TimerTask;

public class GameTimerTask extends TimerTask {

    private final String gameId;
    private final GameRepository gameRepository;

    public GameTimerTask(String game, GameRepository gameRepository) {
        this.gameId = game;
        this.gameRepository = gameRepository;
    }

    @Override
    public void run() {
        this.gameRepository.getGame(gameId)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(Game::endRound)
                .flatMap(this.gameRepository::saveGame)
                .subscribe();
    }
}
