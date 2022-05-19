package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public class GameRound implements Serializable {

    private final Player player;
    private final int currentRound;
    private final String targetWord;
    private int currentGuess = 0;

    private final String[] words = new String[6];
    private final LetterState[][] letterStates = new LetterState[6][5];
    private GameStats gameStats = new GameStats();

    private final long start = System.nanoTime();
    private long finish;

    public GameRound makeGuess(String word) {
        if (this.finish != 0L) {
            return this;
        }
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
            this.endCurrentRound();
        } else {
            this.currentGuess += 1;
        }
        return this;
    }

    private void endCurrentRound() {
        this.finish = System.nanoTime();
        this.gameStats.setTargetWord(targetWord);
        long time = this.finish - this.start / 1000000000;
        this.gameStats.setTimeTaken(time);
        this.gameStats.setScore(100);
    }
}
