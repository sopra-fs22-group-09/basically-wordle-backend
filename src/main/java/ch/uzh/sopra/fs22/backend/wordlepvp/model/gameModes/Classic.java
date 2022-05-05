package ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameLogic;

public class Classic extends GameLogic {

    public Classic() {
        super();
        super.setAmountRounds(1);
        super.setRoundTime(0);
        super.setMaxRounds(1);
        super.setMaxTime(0);
    }

}