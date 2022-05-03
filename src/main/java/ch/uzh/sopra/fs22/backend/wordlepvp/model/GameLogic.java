package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

@Data
@NoArgsConstructor
public abstract class GameLogic implements Game, Serializable {

    private String id;
    private int amountRounds = 1;
    private int roundTime = 0;
    //private int maxAmountRounds = 5;
    //private int maxTimePerRound = 120;

    private String[] repoWords;
    private String[] targetWords;
    private Map<Player, GameRound> game;
    private Map<Player, GameStatus> currentGameStatus = new HashMap<>();

    @Override
    public Game start(Set<Player> players, String[] repoWords) {
        this.repoWords = repoWords;
        this.targetWords = new String[this.amountRounds];
        this.game = new HashMap<>();

        Random r = new SecureRandom();
        Arrays.setAll(targetWords, word -> this.repoWords[r.nextInt(this.repoWords.length)]);
        for (Player player : players) {
            this.game.put(player, new GameRound(player, 0, this.targetWords[0]));
            //this.currentGameStatus.put(player, GameStatus.GUESSING);
        }
        return this;
    }

    @Override
    public GameRound guess(Player player, String word) {

        GameRound currentGameRound = this.game.get(player).makeGuess(word);
        if (this.currentGameStatus.get(player).equals(GameStatus.WAITING)
                || this.currentGameStatus.get(player).equals(GameStatus.FINISHED)) {
            return currentGameRound;
        }
        if (currentGameRound.getFinish() != 0L) {
            this.currentGameStatus.put(player, GameStatus.WAITING);
            if (this.currentGameStatus.entrySet().stream().allMatch(p -> p.getValue().equals(GameStatus.WAITING))) {
                int nextRound = this.game.get(player).getCurrentRound() + 1;
                if (nextRound < amountRounds) {
                    this.game.replaceAll((p, gr) -> new GameRound(p, nextRound, this.targetWords[nextRound]));
                    this.currentGameStatus.replaceAll((p, gs) -> GameStatus.GUESSING);
                    //return this.game.get(player); maybe need that the last guesser also gets updated to the new screen
                } else {
                    this.currentGameStatus.replaceAll((p, gs) -> GameStatus.FINISHED);
                }
            }
        }
        return currentGameRound;
    }

    @Override
    public GameStats concludeGame(Player player) {
        return null;
    }

    @Override
    public GameStatus getGameStatus(Player player) {
        return this.currentGameStatus.get(player);
    }

    @Override
    public void setGameStatus(Player player, GameStatus gameStatus) {
        this.currentGameStatus.put(player, gameStatus);
    }

    @Override
    public GameRound[] getCurrentOpponentGameRounds(Player player) {
        Map<Player, GameRound> tmpGame = game;
        tmpGame.remove(player);
        return tmpGame.values().toArray(new GameRound[0]);
    }
}