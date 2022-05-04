package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class GameStats implements Serializable {

    private String targetWord;

    private int roundsTaken;

    private String timeTaken;

    private int score;

    private int rank;
}
