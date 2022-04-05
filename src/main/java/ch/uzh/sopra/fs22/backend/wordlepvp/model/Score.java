package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.Data;

@Data
public class Score {

    private GameMode mode;
    private int score;

    public Score (GameMode mode) {
        this.mode = mode;
        this.score = 100;
    }
}