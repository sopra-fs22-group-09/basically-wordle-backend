package ch.uzh.sopra.fs22.backend.wordlepvp.model;

public interface Game {

    //TODO: implements max rounds & maxtime for backend ?

    String getId();
    void setId(String id);

    int getAmountRounds();
    void setAmountRounds(int amountRounds);

    int getRoundTime();
    void setRoundTime(int roundTime);

    Game start(String[] repoWords);
    GameRound guess(String word);
    GameStats concludeGame();

    GameRound getGameRound();
}
