package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

@Data
@NoArgsConstructor
public class Classic implements Game, Serializable {

    private String id;

    private int amountRounds = 1;
    private int roundTime = 0;
    private int guesses = 0;
    private GameStatus status;

    private boolean guessed = false;

    private final Random r = new SecureRandom();
    private GameRound gameRound;
    private GameStats gameStats;

    public Game start(Set<Player> players, String[] repoWords) {
        if (players.stream().findFirst().isPresent()) {
            Player player = players.stream().findFirst().get();
            this.gameRound = new GameRound(player, 0, repoWords[this.r.nextInt(repoWords.length)]);
        }
        this.gameStats = new GameStats();
        gameRound.setStart(System.nanoTime());
        return this;
    }

    public Game guess(Player player, String guess) {

        if (this.guessed || guess.length() != 5) {
            return null;
        }
        String[] previousGuesses = gameRound.getWords();
        if (guesses == 0) {
            previousGuesses = new String[6];
        }
            previousGuesses[guesses] = guess;
            gameRound.setWords(previousGuesses);

        String[] guess_chars = guess.split("");
        String[] targetWord_chars = gameRound.getTargetWord().split("");
        LetterState[][] letterState = gameRound.getLetterStates();

        if (guesses == 0) {
            letterState = new LetterState[6][5];
        }

        //letterState[0][0] = LetterState.INWORD;
        for (int j=0; j < guess_chars.length; j++) {

                if (guess_chars[j].equals(targetWord_chars[j])) {
                    letterState[guesses][j] = LetterState.CORRECTPOSITION;
                }
                else {
                    letterState[guesses][j] = LetterState.WRONG;
                }
                for (int i=0; i < targetWord_chars.length; i++) {
                    if (guess_chars[j].equals(targetWord_chars[i]) && i != j && letterState[guesses][j] != LetterState.CORRECTPOSITION) {
                        letterState[guesses][j] = LetterState.INWORD;
                        break;
                    }
                }

        }
        if (Objects.equals(guess, gameRound.getTargetWord())) {
            this.guessed = true;
            this.endGame();
        }
        /*for (int i=0; i < letterState[guesses].length; i++) {
            if (letterState[guesses][i] != LetterState.CORRECTPOSITION) {
                break;
            }
            this.guessed = true;
            this.endGame();
        }*/

        gameRound.setLetterStates(letterState);
        guesses++;
        if (guesses >= 6 && !this.guessed) {
            this.endGame();
        }

        return this;
    }

    // if game is lost/won
    public void endGame() {
        gameRound.setFinish(System.nanoTime());
    }

    // player wins the game
    public GameStats concludeGame(Player player) {
        if (gameRound.getFinish() == 0L) {
            return null;
        }
        LetterState[][] letterState = gameRound.getLetterStates();
        boolean won = false;
        long elapsedTime = (gameRound.getFinish() - gameRound.getStart()) / 1000000000;
        long hours = elapsedTime / 3600;
        long minutes = (elapsedTime % 3600) / 60;
        long seconds = elapsedTime % 60;
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        gameStats.setTimeTaken(timeString);
        gameStats.setRoundsTaken(guesses);
        gameStats.setTargetWord(gameRound.getTargetWord());

        for (int i=0; i < letterState[guesses].length; i++) {
            if (letterState[guesses-1][i] != LetterState.CORRECTPOSITION) {
                break;
            }
            won = true;
        }
        if (guesses <= 6 && won) {
            gameStats.setRank(1);
            gameStats.setScore(100 / guesses);
        }
        else {
            gameStats.setRank(0);
            gameStats.setScore(0);
        }
        return gameStats;
        //conclude stats and show them to the player
        // return all infos in model GameStats: time taken, rounds taken, targetWord, info if player has won, score
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