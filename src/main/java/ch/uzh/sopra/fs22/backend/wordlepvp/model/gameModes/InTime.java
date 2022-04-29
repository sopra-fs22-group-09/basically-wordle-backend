package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStats;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InTime implements Game, Serializable {

    private String id;
    private int amountRounds = 1;
    private int roundTime = 0;

    Random r = new Random();
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

    public GameStats concludeGame() {
        return null;
    }

    public Game newGameRound(Player player) {
        return null;
    }

    public GameRound getCurrentGameRound(Player player) {
        return null;
    }

    public GameRound[] getCurrentOpponentGameRounds(Player player) {
        return null;
    }
}