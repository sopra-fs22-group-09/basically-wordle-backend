package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class GameStats implements Serializable {

    private String targetWord;

    private int roundsTaken;

    private long timeTaken;

    private int score;

    private List<Player> ranking;
}