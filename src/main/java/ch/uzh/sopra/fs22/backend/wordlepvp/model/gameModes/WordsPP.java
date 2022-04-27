package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordsPP implements Game, Serializable {

    private String id;

    private int amountRounds = 1;
    private int roundTime = 0;

    //Watch out for the getGameRound for PvP: Manually return one from the GameRound Array
    private GameRound gameRound;

    public Game start(String[] repoWords) {
        return this;
    }

    public GameRound guess(String word) {
        return null;
    }

    public GameStats concludeGame() {
        return null;
    }
}