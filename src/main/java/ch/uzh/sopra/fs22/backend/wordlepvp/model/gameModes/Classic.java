package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classic implements Game, Serializable {

    public int amountRounds = 1;
    public int roundTime = 0;

    public void start() {
        System.out.println("TEST ME BITCH");
    }

    public Mono<GameRound> guess(String word) {
        return null;
    }
}