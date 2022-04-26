package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.LetterState;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
public class Classic implements Game, Serializable {

    public int amountRounds = 1;
    public int roundTime = 0;

    private int guesses = 0;

    private GameRound gameRound;

    public Game start(String[] repoWords) {
        this.gameRound = new GameRound(repoWords);
        gameRound.setStart(System.nanoTime());
        return this;
    }

    public GameRound guess(String guess) {

        String[] previousGuesses = gameRound.getWords();
        if (guesses == 0) {
            previousGuesses = new String[6];
        }
            previousGuesses[guesses] = guess;
            gameRound.setWords(previousGuesses);

        if (Objects.equals(guess, gameRound.getTargetWord())) {
            this.concludeGame();
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

                if (guess_chars[j].equals(targetWord_chars[j])) {
                    letterState[guesses][j] = LetterState.CORRECTPOSITION;
                }
                else {
                    letterState[guesses][j] = LetterState.WRONG;
                }
                for (int i=0; i < targetWord_chars.length; i++) {
                    if (guess_chars[j].equals(targetWord_chars[i]) && i != j) {
                        letterState[guesses][j] = LetterState.INWORD;
                        break;
                    }
                }

        }
        for (int i=0; i < letterState[guesses].length; i++) {
            if (letterState[guesses][i] != LetterState.CORRECTPOSITION) {
                break;
            }
            this.concludeGame();
        }
        gameRound.setLetterStates(letterState);
        guesses++;

        return gameRound; //TODO change
    }

    // if game is lost
    public void endGame() {
        gameRound.setFinish(System.nanoTime());

    }

    // player wins the game
    public void concludeGame() {
        //conclude stats and show them to the player
    }
}