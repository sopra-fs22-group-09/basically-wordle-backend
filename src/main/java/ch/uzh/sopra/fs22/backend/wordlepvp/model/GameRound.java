package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Random;

@Data
public class GameRound implements Serializable {

    private String[] repoWords;
    private String targetWord;

    public GameRound(String[] repoWords) {
        this.repoWords = repoWords;
        Random r = new Random();
        this.targetWord = repoWords[r.nextInt(repoWords.length)];
    }

    private String[] words;
    private LetterState[][] letterStates;

    private long start;
    private long finish;

}
