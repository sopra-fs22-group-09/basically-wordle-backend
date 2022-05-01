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
    Game guess(Player player, String word);
    GameStats concludeGame(Player player);

    GameStatus getStatus();
    void setStatus(GameStatus status);
    PlayerStatus getPlayerStatus(Player player);
    void setPlayerStatus(Player player, PlayerStatus playerStatus);
    boolean playersSynced();

    GameRound getCurrentGameRound(Player player);
    GameRound[] getCurrentOpponentGameRounds(Player player);
}
