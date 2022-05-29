package ch.uzh.sopra.fs22.backend.wordlepvp.logic;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public abstract class GameLogic implements Game, Serializable {

    private String id;
    private int amountRounds = 0;
    private int roundTime = 0;
    private int maxRounds = 10;
    private int maxTime = 180;

    private String[] repoWords;
    private String[] allowedWords;
    private String[] targetWords;
    private Map<Player, GameRound> game = new HashMap<>();
    private Map<Player, GameStatus> currentGameStatus = new HashMap<>();
    private Map<Player, GameStats> gameStats = new HashMap<>();
    private Map<Player, Integer> ranking = new HashMap<>();

    @Override
    public Game start(Set<Player> players, String[] repoWords, String[] allowedWords) {
        this.repoWords = repoWords;
        this.allowedWords = allowedWords;
        if (this.maxRounds == 0) {
            this.targetWords = new String[50];
        } else {
            this.targetWords = new String[this.amountRounds];
        }

        Random r = new SecureRandom();
        Arrays.setAll(targetWords, word -> this.repoWords[r.nextInt(this.repoWords.length)]);
        for (Player player : players) {
            this.game.put(player, new GameRound(player, 0, this.targetWords[0]));
            GameStats gameStats = new GameStats();
            gameStats.setScore(0);
            this.gameStats.put(player, gameStats);
            this.ranking.put(player, 0);
            updateTargetWord(player);
        }
        this.gameStats.forEach((p, gs) -> {
            gs.setRanking(this.ranking.keySet().stream().toList());
            this.gameStats.put(p, gs);
        });
        return this;
    }

    @Override
    public GameRound guess(Player player, String word) {
        GameRound currentGameRound = this.game.get(player).makeGuess(Arrays.asList(this.allowedWords).contains(word) ? word : "");

        if (this.currentGameStatus.get(player) == null) {
            System.err.println("The Gamestatus was null for whatever reason. It got reinitialized.");
            this.currentGameStatus.put(player, GameStatus.GUESSING);
        }
        if (this.currentGameStatus.get(player).equals(GameStatus.WAITING)
                || this.currentGameStatus.get(player).equals(GameStatus.FINISHED)) {
            return currentGameRound;
        }
        if (currentGameRound.isFinished()) {
            this.saveStats(player);
            this.currentGameStatus.put(player, GameStatus.WAITING);
            if (this.maxRounds == 0) { //Words++
                int nextRound = currentGameRound.getCurrentRound() + 1;
                this.game.put(player, new GameRound(player, nextRound, this.targetWords[nextRound]));
                this.currentGameStatus.put(player, GameStatus.GUESSING);
            }
        }
        return currentGameRound;
    }

    @Override
    public void endRound() {
        currentGameStatus.replaceAll((p, gs) -> GameStatus.WAITING);

        GameRound currentGameRound;
        Optional<GameRound> currentGameRoundOptional = this.game.values().stream().findFirst();
        if (currentGameRoundOptional.isPresent()) {
            currentGameRound = currentGameRoundOptional.get();
        } else {
            return;
        }
        int nextRound = currentGameRound.getCurrentRound() + 1;
        if (this.maxTime == 0) { //Classic
            this.currentGameStatus.replaceAll((p, gs) -> GameStatus.FINISHED);
        } else if (nextRound < this.amountRounds) {
            this.gameStats.forEach((p, gs) -> updateTargetWord(p));
            this.game.replaceAll((p, gr) -> new GameRound(p, nextRound, this.targetWords[nextRound]));
        } else {
            this.gameStats.forEach((p, gs) -> updateTargetWord(p));
            this.currentGameStatus.replaceAll((p, gs) -> GameStatus.FINISHED);
        }
    }

    @Override
    public GameStats concludeGame(Player player) {
        return this.gameStats.get(player);
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
        gameStats.setTargetWord(this.targetWords[this.game.get(player).getCurrentRound()]);
        gameStats.setTimeTaken(gameStats.getTimeTaken() + roundStats.getTimeTaken());
        gameStats.setRoundsTaken(this.game.get(player).getCurrentRound() + 1);
        gameStats.setScore(gameStats.getScore() + roundStats.getScore());
        this.ranking.replace(player, gameStats.getScore());

        Map<Player, Integer> ranks = this.ranking.entrySet().stream()
                .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (p, s) -> p, LinkedHashMap::new));
        this.gameStats.forEach((p, gs) -> {
            gs.setRanking(ranks.keySet().stream().toList());
            this.gameStats.put(p, gs);
        });
    }

    private void updateTargetWord(Player player) {
        GameStats gameStats = this.gameStats.get(player);
        gameStats.setTargetWord(this.game.get(player).getTargetWord());
        this.gameStats.put(player, gameStats);
    }
}