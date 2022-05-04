package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import java.util.Set;

public interface Game {

    //TODO: implements max rounds & maxtime for backend ?

    String getId();
    void setId(String id);

    void setAmountRounds(int amountRounds);
    void setRoundTime(int roundTime);

    Game start(Set<Player> players, String[] repoWords);
    GameRound guess(Player player, String word);
    GameStats concludeGame(Player player);

    GameStatus getGameStatus(Player player);
    void setGameStatus(Player player, GameStatus status);

    Set<Player> getPlayers();

    GameRound[] getCurrentOpponentGameRounds(Player player);
}
