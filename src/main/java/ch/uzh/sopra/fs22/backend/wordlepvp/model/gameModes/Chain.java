package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chain implements Game, Serializable {

    private String id;
    private int amountRounds = 1;
    private int roundTime = 0;
    private GameStatus status;

    private final Random r = new SecureRandom();
    private String[] repoWords;
    private String[] targetWords;
    private Map<Player, GameRound[]> game;
    private Map<Player, GameRound> currentGameRound;

    public Game start(Set<Player> players, String[] repoWords) {
        return this;
    }

    public Game guess(Player player, String word) {
        return null;
    }

    public GameStats concludeGame(Player player) {
        return null;
    }

    public Game newGameRound(Player player) {
        return null;
    }

    public GameRound getCurrentGameRound(Player player) {
        return null;
    }

    public GameStatus getCurrentGameStatus(Player player) {
        return null;
    }

    public GameRound[] getCurrentOpponentGameRounds(Player player) {
        return null;
    }
}