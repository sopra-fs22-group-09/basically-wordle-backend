package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import java.util.Set;
import java.util.Timer;

public interface Game {

    String getId();
    void setId(String id);

    void setAmountRounds(int amountRounds);
    void setRoundTime(int roundTime);
    int getRoundTime();
    int getMaxRounds();
    int getMaxTime();

    Game start(Set<Player> players, String[] repoWords);
    GameRound guess(Player player, String word);
    GameRound endRound();
    GameStats concludeGame(Player player);

    GameStatus getGameStatus(Player player);
    void setGameStatus(Player player, GameStatus status);

    Set<Player> getPlayers();

    GameRound[] getCurrentOpponentGameRounds(Player player);
}
