package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.WordsRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chain implements Game, Serializable {

    public int amountRounds = 1;
    public int roundTime = 0;

    private GameRound gameRound;

    public void start() {

    }

    public GameRound guess(String word) {
        return null;
    }
}
