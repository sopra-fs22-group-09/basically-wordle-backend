package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameLogic;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chain extends GameLogic implements Game, Serializable {

    public int amountRounds = 1;
    public int roundTime = 0;

}
