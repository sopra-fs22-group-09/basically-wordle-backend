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
    private int amountRounds = 0;
    private int roundTime = 0;
    private int maxRounds = 10;
    private int maxTime = 180;

    private String[] repoWords;
    private String[] targetWords;
    private Map<Player, GameRound> game = new HashMap<>();
    private Map<Player, GameStatus> currentGameStatus = new HashMap<>();
    private Map<Player, GameStats> gameStats = new HashMap<>();

    @Override
    public Game start(Set<Player> players, String[] repoWords) {
        this.repoWords = repoWords;
        if (this.maxRounds == 0) {
            this.targetWords = new String[50];
        } else {
            this.targetWords = new String[this.amountRounds];
        }

        Random r = new SecureRandom();
        Arrays.setAll(targetWords, word -> this.repoWords[r.nextInt(this.repoWords.length)]);
        for (Player player : players) {
            this.game.put(player, new GameRound(player, 0, this.targetWords[0]));
        }

        return this;
    }

    @Override
    public GameRound guess(Player player, String word) {
        GameRound currentGameRound = this.game.get(player).makeGuess(word);
        // FIXME: Handle NPE
        if (this.currentGameStatus.get(player).equals(GameStatus.WAITING)
                || this.currentGameStatus.get(player).equals(GameStatus.FINISHED)) {
            return currentGameRound;
        }
        if (currentGameRound.getFinish() != 0L) {
            if (this.maxRounds == 0) {
                if (this.amountRounds == 0) {
                    endRound();
                }
                int nextRound = currentGameRound.getCurrentRound() + 1;
                this.game.put(player, new GameRound(player, nextRound, this.targetWords[nextRound]));
            } else {
                this.currentGameStatus.put(player, GameStatus.WAITING);
            }
        }
        return currentGameRound;
    }

    @Override
    public GameRound endRound() {
        currentGameStatus.replaceAll((p, gs) -> GameStatus.WAITING);
        if (this.game.values().stream().findFirst().isEmpty()) {
            return null;
        }
        GameRound currentGameRound = this.game.values().stream().findFirst().get();
        int nextRound = currentGameRound.getCurrentRound() + 1;
        if (this.maxRounds == 0) {
            this.currentGameStatus.replaceAll((p, gs) -> GameStatus.FINISHED);
        } else if (nextRound < this.amountRounds) {
            this.game.forEach((p, g) -> this.saveStats(p));
            this.game.replaceAll((p, gr) -> new GameRound(p, nextRound, this.targetWords[nextRound]));
            this.currentGameStatus.replaceAll((p, gs) -> GameStatus.GUESSING);
            currentGameRound = this.game.get(currentGameRound.getPlayer()); //maybe need that the last guesser also gets updated to the new screen
        } else {
            this.currentGameStatus.replaceAll((p, gs) -> GameStatus.FINISHED);
        }
        return currentGameRound;
    }

    @Override
    public GameStats concludeGame(Player player) {
        if (this.currentGameStatus.get(player).equals(GameStatus.WAITING)) {
            return this.game.get(player).getGameStats();
        } else if (this.currentGameStatus.get(player).equals(GameStatus.FINISHED)) {
            return this.gameStats.get(player);
        } else {
            return null;
        }
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
        Map<Player, GameRound> tmpGame = new HashMap<>(Map.copyOf(game));
        tmpGame.remove(player);
        return tmpGame.values().toArray(new GameRound[0]);
    }

    @Override
    public Set<Player> getPlayers() {
        if (currentGameStatus != null) {
            return currentGameStatus.keySet();
        }
        return null;
    }

    private void saveStats(Player player) {
        GameStats roundStats = this.game.get(player).getGameStats();
        GameStats gameStats = this.gameStats.get(player);
        gameStats.setTimeTaken(gameStats.getTimeTaken() + roundStats.getTimeTaken());
        gameStats.setScore(gameStats.getScore() + roundStats.getScore());
        this.gameStats.put(player, gameStats);
    }
}