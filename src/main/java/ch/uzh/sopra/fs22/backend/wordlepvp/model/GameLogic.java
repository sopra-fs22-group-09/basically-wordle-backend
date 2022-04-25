package ch.uzh.sopra.fs22.backend.wordlepvp.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
//@RedisHash
@NoArgsConstructor
@AllArgsConstructor
public abstract class GameLogic {

    protected Map<UUID, GameRound[]> game;

    protected int timeElapsed;

    protected String targetWord;

    public void concludeGame(){}

    public void endGame(){}
}
