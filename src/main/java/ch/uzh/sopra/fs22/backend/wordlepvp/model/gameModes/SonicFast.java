package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

@Data
@NoArgsConstructor
public class SonicFast implements Game, Serializable {

    private String id;
    private int amountRounds = 1;
    private int roundTime = 0;
    private GameStatus status;

    private final Random r = new SecureRandom();
    private String[] repoWords;
    private String[] targetWords;
    private Map<Player, GameRound[]> game;
    private Map<Player, GameRound> currentGameRound;

    private Map<Player, GameStats> gameStats;
    private Map<Player, Integer[]> guesses; //(add mapping to gameround) getCurrentGameround()
    private Map<Player, Boolean[]> guessed; //add mapping to player

    public Game start(Set<Player> players, String[] repoWords) {
        this.repoWords = repoWords;
        this.targetWords = new String[this.amountRounds];
        this.game = new HashMap<>();
        this.currentGameRound = new HashMap<>();
        this.gameStats = new HashMap<>();
        this.guesses = new HashMap<>();
        this.guessed = new HashMap<>();
        for (int i = 0; i < amountRounds; i++) {
            this.targetWords[i] = repoWords[this.r.nextInt(repoWords.length)];
        }

        for (Player player : players) {
            GameRound[] gameRounds = new GameRound[amountRounds];
            Integer[] guesses = new Integer[amountRounds];
            Boolean[] guessed = new Boolean[amountRounds];
            for (int i = 0; i < this.amountRounds; i++) {
                gameRounds[i] = new GameRound(player, i, this.targetWords[i]);
                guesses[i] = 0;
                guessed[i] = false;
            }
            this.guesses.put(player, guesses);
            this.guessed.put(player, guessed);
            this.game.put(player, gameRounds);
            this.currentGameRound.put(player, this.game.get(player)[0]);
            this.currentGameRound.get(player).setStart(System.nanoTime());
        }
        return this;
    }

    public Game guess(Player player, String guess) {
/*        if (Objects.equals(guess, this.targetWords[this.currentGameRound.get(player).getCurrentRound()])) {
            this.currentGameRound.get(player).setFinish(System.nanoTime());
        }*/

        if (this.guessed.get(player)[this.currentGameRound.get(player).getCurrentRound()] || guess.length() != 5) {
            return this;
        }
        String[] previousGuesses = this.currentGameRound.get(player).getWords();
        if (this.guesses.get(player)[this.currentGameRound.get(player).getCurrentRound()] == 0) {
            previousGuesses = new String[6];
        }
        previousGuesses[this.guesses.get(player)[this.currentGameRound.get(player).getCurrentRound()]] = guess;
        this.currentGameRound.get(player).setWords(previousGuesses);

        String[] guess_chars = guess.split("");
        String[] targetWord_chars = this.currentGameRound.get(player).getTargetWord().split("");
        LetterState[][] letterState = this.currentGameRound.get(player).getLetterStates();

        if (this.guesses.get(player)[this.currentGameRound.get(player).getCurrentRound()] == 0) {
            letterState = new LetterState[6][5];
        }

        //letterState[0][0] = LetterState.INWORD;
        for (int j=0; j < guess_chars.length; j++) {

            if (guess_chars[j].equals(targetWord_chars[j])) {
                letterState[this.guesses.get(player)[this.currentGameRound.get(player).getCurrentRound()]][j] = LetterState.CORRECTPOSITION;
            }
            else {
                letterState[this.guesses.get(player)[this.currentGameRound.get(player).getCurrentRound()]][j] = LetterState.WRONG;
            }
            for (int i=0; i < targetWord_chars.length; i++) {
                if (guess_chars[j].equals(targetWord_chars[i]) && i != j && letterState[this.guesses.get(player)[this.currentGameRound.get(player).getCurrentRound()]][j] != LetterState.CORRECTPOSITION) {
                    letterState[this.guesses.get(player)[this.currentGameRound.get(player).getCurrentRound()]][j] = LetterState.INWORD;
                    break;
                }
            }

        }
        if (Objects.equals(guess, this.currentGameRound.get(player).getTargetWord())) {
            Boolean[] updatedGuessed = this.guessed.get(player);
            updatedGuessed[this.currentGameRound.get(player).getCurrentRound()] = true;
            this.guessed.put(player, updatedGuessed);
          //  this.endGame(player);
        }

        this.currentGameRound.get(player).setLetterStates(letterState);
        Integer[] updatedGuesses = this.guesses.get(player);
        updatedGuesses[this.currentGameRound.get(player).getCurrentRound()] = updatedGuesses[this.currentGameRound.get(player).getCurrentRound()] + 1;
        this.guesses.put(player, updatedGuesses);
        if (this.guesses.get(player)[this.currentGameRound.get(player).getCurrentRound()] >= 6) {
            this.endGame(player);
        }
        if (this.guessed.get(player)[this.currentGameRound.get(player).getCurrentRound()] && this.currentGameRound.get(player).getCurrentRound() <= (amountRounds - 2)) {
            this.endGame(player);
        }
        if (this.guessed.get(player)[this.currentGameRound.get(player).getCurrentRound()] && this.currentGameRound.get(player).getCurrentRound() > (amountRounds - 2)) {
            this.endGame(player);
        }
        return this;
    }

    public GameStats concludeGame(Player player) {
        if (this.currentGameRound.get(player).getFinish() == 0L) {
            return null;
        }
        LetterState[][] letterState = this.currentGameRound.get(player).getLetterStates();
        int numberWon = 0;
        int roundsTaken = 0;
        for (int i = 0; i < this.guesses.get(player).length; i++) {
            roundsTaken += this.guesses.get(player)[i];
        }
        long elapsedTime = (this.currentGameRound.get(player).getFinish() - this.game.get(player)[0].getStart()) / 1000000000;
        long hours = elapsedTime / 3600;
        long minutes = (elapsedTime % 3600) / 60;
        long seconds = elapsedTime % 60;
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        this.gameStats.get(player).setTimeTaken(timeString);
        this.gameStats.get(player).setRoundsTaken(roundsTaken);
        if (this.amountRounds == 1) {
            this.gameStats.get(player).setTargetWord(this.currentGameRound.get(player).getTargetWord());
        }

        for (int i=0; i < this.guessed.get(player).length; i++) {
            boolean isGuessed = this.guessed.get(player)[i];
            if (isGuessed) {
                numberWon += 1;
            }
        }
        if (numberWon >= 1) {
            this.gameStats.get(player).setRank(numberWon);
            this.gameStats.get(player).setScore((amountRounds * 100) / (roundsTaken / amountRounds));
        }
        else {
            this.gameStats.get(player).setRank(0);
            this.gameStats.get(player).setScore(0);
        }
        return this.gameStats.get(player);
        //conclude stats and show them to the player
        // return all infos in model GameStats: time taken, rounds taken, targetWord, info if player has won, score
    }

    public void endGame(Player player) {
        this.currentGameRound.get(player).setFinish(System.nanoTime());

        int currentRound = this.currentGameRound.get(player).getCurrentRound();
        boolean allDone = true;
        for (Map.Entry<Player, Integer[]> entry : this.guesses.entrySet()) {
            if (!this.guessed.get(entry.getKey())[currentRound] && entry.getValue()[currentRound] < 6) {
                allDone = false;
                break;
            }
        }
        if (allDone) {
            int nextRound = this.currentGameRound.get(player).getCurrentRound() + 1;
            if (nextRound < amountRounds) {
                this.currentGameRound.replaceAll((k, v) -> this.game.get(k)[nextRound]);
            }
        }
    }

    public GameRound getCurrentGameRound(Player player) {
        return this.currentGameRound.get(player);
    }

    public GameStatus getCurrentGameStatus(Player player) {
        int currentRound = this.currentGameRound.get(player).getCurrentRound();
        if (currentRound >= amountRounds) {
            return GameStatus.FINISHED;
        }
        if (this.guessed.get(player)[currentRound]) {
            for (Map.Entry<Player, Boolean[]> entry : this.guessed.entrySet()) {
                if (!entry.getValue()[currentRound]) {
                    return GameStatus.WAITING;
                }
            }
        }
        return GameStatus.GUESSING;
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