package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import reactor.core.publisher.Mono;

public interface Game {

    //TODO: implements max rounds & maxtime for backend ?

    int getAmountRounds();
    void setAmountRounds(int amountRounds);

    int getRoundTime();
    void setRoundTime(int roundTime);

    Game start(String[] repoWords);
    GameRound guess(String word);

}
