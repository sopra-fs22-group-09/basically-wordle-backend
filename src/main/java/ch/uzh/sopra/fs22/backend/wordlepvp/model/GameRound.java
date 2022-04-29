package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class GameRound implements Serializable {

    private Player player;
    private int currentRound;
    private String targetWord;

    public GameRound(Player player, int currentRound, String targetWord) {
        this.player = player;
        this.currentRound = currentRound;
        this.targetWord = targetWord;
    }

    private String[] words;
    private LetterState[][] letterStates;

    private long start;
    private long finish;

}
