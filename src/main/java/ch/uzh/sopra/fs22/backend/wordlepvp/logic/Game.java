package ch.uzh.sopra.fs22.backend.wordlepvp.logic;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStats;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStatus;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;

import java.util.Set;

public interface Game {

    String getId();
    void setId(String id);

    void setAmountRounds(int amountRounds);
    void setRoundTime(int roundTime);
    int getRoundTime();
    int getMaxRounds();
    int getMaxTime();

    Game start(Set<Player> players, String[] repoWords, String[] allowedWords);
    GameRound guess(Player player, String word);
    void endRound();
    GameStats concludeGame(Player player);

    GameStatus getGameStatus(Player player);
    void setGameStatus(Player player, GameStatus status);

    Set<Player> getPlayers();

    GameRound[] getCurrentOpponentGameRounds(Player player);
}
