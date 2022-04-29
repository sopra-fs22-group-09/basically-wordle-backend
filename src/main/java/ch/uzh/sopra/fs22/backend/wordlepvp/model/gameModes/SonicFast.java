package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStats;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SonicFast implements Game, Serializable {

    private String id;
    private int amountRounds = 1;
    private int roundTime = 0;

    Random r = new Random();
    private String[] repoWords;
    private String[] targetWords;
    private Map<Player, GameRound[]> game;
    private Map<Player, GameRound> currentGameRound;

    public Game start(Set<Player> players, String[] repoWords) {
        this.repoWords = repoWords;
        this.targetWords = new String[this.amountRounds];
        this.game = new HashMap<>();
        this.currentGameRound = new HashMap<>();
        for (int i = 0; i < amountRounds; i++) {
            this.targetWords[i] = repoWords[this.r.nextInt(repoWords.length)];
        }

        for (Player player : players) {
            GameRound[] gameRounds = new GameRound[amountRounds];
            for (int i = 0; i < this.amountRounds; i++) {
                gameRounds[i] = new GameRound(player, i, this.targetWords[i]);
            }
            this.game.put(player, gameRounds);
            this.currentGameRound.put(player, this.game.get(player)[0]);
        }
        return this;
    }

    public Game guess(Player player, String word) {
        if (Objects.equals(word, this.targetWords[this.currentGameRound.get(player).getCurrentRound()])) {
            this.currentGameRound.get(player).setFinish(333);
        }
        return this;
    }

    public GameStats concludeGame() {
        return null;
    }

    public Game newGameRound(Player player) {
        if (this.currentGameRound.get(player).getFinish() != 0) {
            int nextRound = this.currentGameRound.get(player).getCurrentRound() + 1;
            this.currentGameRound.put(player, this.game.get(player)[nextRound]);
        }
        return this;
    }

    public GameRound getCurrentGameRound(Player player) {
        return this.currentGameRound.get(player);
    }

    public GameRound[] getCurrentOpponentGameRounds(Player player) {
        GameRound[] opponentRounds = new GameRound[currentGameRound.size() - 1];
        int i = 0;
        for (Map.Entry<Player, GameRound> entry : currentGameRound.entrySet()) {
            if (!Objects.equals(entry.getKey().getId(), player.getId())) {
                opponentRounds[i] = entry.getValue();
                i++;
            }
        }
        return opponentRounds;
    }
}