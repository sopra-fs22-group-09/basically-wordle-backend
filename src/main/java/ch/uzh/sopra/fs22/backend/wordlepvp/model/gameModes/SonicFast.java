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
        }
        return this;
    }

    public Game guess(Player player, String guess) {
/*        if (Objects.equals(guess, this.targetWords[this.currentGameRound.get(player).getCurrentRound()])) {
            this.currentGameRound.get(player).setFinish(System.nanoTime());
        }*/
        if (this.currentGameRound.get(player).getFinish() != 0) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "The GameRound has already finished.");
        }

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
            this.endGame(player);
        }
        /*for (int i=0; i < letterState[guesses].length; i++) {
            if (letterState[guesses][i] != LetterState.CORRECTPOSITION) {
                break;
            }
            this.guessed = true;
            this.endGame();
        }*/

        this.currentGameRound.get(player).setLetterStates(letterState);
        Integer[] updatedGuesses = this.guesses.get(player);
        updatedGuesses[this.currentGameRound.get(player).getCurrentRound()] = updatedGuesses[this.currentGameRound.get(player).getCurrentRound()] + 1;
        this.guesses.put(player, updatedGuesses);
        if (this.guesses.get(player)[this.currentGameRound.get(player).getCurrentRound()] >= 6 && !this.guessed.get(player)[this.currentGameRound.get(player).getCurrentRound()]) {
            this.endGame(player);
        }
        return this;
    }

    public GameStats concludeGame() {
        return null;
    }

    public void endGame(Player player) {
        this.currentGameRound.get(player).setFinish(System.nanoTime());
    }

    public Game newGameRound(Player player) {
        if (this.currentGameRound.get(player).getFinish() != 0) {
            int nextRound = this.currentGameRound.get(player).getCurrentRound() + 1;
            if (nextRound >= amountRounds) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "The Game has already finished.");
            }
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