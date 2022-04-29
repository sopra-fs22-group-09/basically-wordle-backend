package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import java.util.Set;

public interface Game {

    //TODO: implements max rounds & maxtime for backend ?

    String getId();
    void setId(String id);

    int getAmountRounds();
    void setAmountRounds(int amountRounds);

    int getRoundTime();
    void setRoundTime(int roundTime);

    Game start(Set<Player> players, String[] repoWords);
    Game newGameRound(Player player);
    Game guess(Player player, String word);
    GameStats concludeGame();

    GameRound getCurrentGameRound(Player player);
    GameRound[] getCurrentOpponentGameRounds(Player player);
}
