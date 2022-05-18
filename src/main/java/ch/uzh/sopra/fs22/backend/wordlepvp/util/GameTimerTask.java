package ch.uzh.sopra.fs22.backend.wordlepvp.util;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;

import java.util.TimerTask;

public class GameTimerTask extends TimerTask {

    private final Game game;
    private final GameRepository gameRepository;

    public GameTimerTask(Game game, GameRepository gameRepository) {
        this.game = game;
        this.gameRepository = gameRepository;
    }

    @Override
    public void run() {
        this.game.endRound();
        this.gameRepository.saveGame(this.game).subscribe();
    }
}
