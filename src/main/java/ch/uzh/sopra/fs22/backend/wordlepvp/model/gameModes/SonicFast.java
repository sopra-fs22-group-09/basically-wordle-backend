package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameLogic;

public class SonicFast extends GameLogic {

    public SonicFast() {
        super();
        super.setAmountRounds(3);
        super.setRoundTime(120);
        super.setMaxRounds(10);
        super.setMaxTime(240);
    }

}