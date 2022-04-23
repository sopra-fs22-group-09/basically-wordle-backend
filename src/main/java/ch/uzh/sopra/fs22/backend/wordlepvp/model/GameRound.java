package ch.uzh.sopra.fs22.backend.wordlepvp.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
//@RedisHash
@NoArgsConstructor
@AllArgsConstructor
public class GameRound {

    private String targetWord;

    private Character[][] words;

    private LetterState[][] letterStates;

    private int timeTaken;
}
