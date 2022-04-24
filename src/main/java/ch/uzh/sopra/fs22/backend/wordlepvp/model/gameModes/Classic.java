package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.LetterState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classic implements Game, Serializable {

    public int amountRounds = 1;
    public int roundTime = 0;

    private int guesses = 0;

    private GameRound gameRound;


    public void start() {
        gameRound = new GameRound();
        gameRound.setStart(System.nanoTime());
    }

    public void guess(String guess) {
        String[] previousGuesses = gameRound.getWords();
        if (guesses == 0) {
            previousGuesses = new String[6];
        }
            previousGuesses[guesses] = guess;
            gameRound.setWords(previousGuesses);

        if (Objects.equals(guess, gameRound.getTargetWord())) {
            this.endGame();
        }
        if (guesses >= 6) {
            this.endGame();
        }
        String[] guess_chars = guess.split("");
        String[] targetWord_chars = gameRound.getTargetWord().split("");
        LetterState[][] letterState = gameRound.getLetterStates();

        if (guesses == 0) {
            letterState = new LetterState[6][5];
        }

        //letterState[0][0] = LetterState.INWORD;
        for (int j=0; j < guess_chars.length; j++) {
            for (int i=0; i < targetWord_chars.length; i++) {
                if (guess_chars[j].equals(targetWord_chars[i]) && i == j) {
                    letterState[guesses][j] = LetterState.CORRECTPOSITION;
                }
                else if (guess_chars[j].equals(targetWord_chars[i])) {
                    letterState[guesses][j] = LetterState.INWORD;
                }
                else {
                    letterState[guesses][j] = LetterState.WRONG;
                }
            }
        }
        for (int i=0; i < letterState[guesses].length; i++) {
            if (letterState[guesses][i] != LetterState.CORRECTPOSITION) {
                break;
            }
            this.endGame();
        }
        gameRound.setLetterStates(letterState);
        guesses++;
    }

    public void endGame() {
        gameRound.setFinish(System.nanoTime());

    }

}