package ch.uzh.sopra.fs22.backend.wordlepvp.logic;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStats;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.LetterState;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public class GameRound implements Serializable {

    private final Player player;
    private final int currentRound;
    private final String targetWord;
    private int currentGuess = 0;
    private boolean finished = false;
    private boolean guessed = false;

    private final String[] words = new String[6];
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private final LetterState[][] letterStates = new LetterState[6][5];
    private final GameStats gameStats = new GameStats();

    private final long start = System.nanoTime();
    private long finish;

    public GameRound makeGuess(String word) {
        if (this.finished || word.equals("") || Arrays.asList(this.words).contains(word)) return this;

        this.words[this.currentGuess] = word;
        for (int i = 0; i < word.length(); i++) {
            if (this.targetWord.charAt(i) == word.charAt(i)) {
                this.letterStates[this.currentGuess][i] = LetterState.CORRECTPOSITION;
            } else if (targetWord.contains(Character.toString(word.charAt(i)))) {
                this.letterStates[this.currentGuess][i] = LetterState.INWORD;
            } else {
                this.letterStates[this.currentGuess][i] = LetterState.WRONG;
            }
        }
        if (this.targetWord.equals(word) || this.currentGuess >= 5) {
            if (this.targetWord.equals(word)) {
                this.guessed = true;
            }
            this.endCurrentRound();
        } else {
            this.currentGuess += 1;
        }
        return this;
    }

    private void endCurrentRound() {
        this.finished = true;
        this.finish = System.nanoTime();
        this.gameStats.setTargetWord(targetWord);
        long time = (this.finish - this.start) / 1000000000;
        this.gameStats.setTimeTaken(time);
        int score;
        if (!this.guessed) {
            score = 1;
        } else {
            score = (int) (100 - ((int) Math.pow(currentGuess, 1.5) * 5) - (((time % 3600) / 60 ) / 5));
        }
        this.gameStats.setScore(score);
    }
}
