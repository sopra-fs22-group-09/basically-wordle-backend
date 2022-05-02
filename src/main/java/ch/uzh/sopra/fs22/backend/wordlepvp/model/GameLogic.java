package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@RequiredArgsConstructor
public abstract class GameLogic implements Game {

    private String id;
    private int amountRounds = 1;
    private int roundTime = 0;

    @Override
    public Game start(Set<Player> players, String[] repoWords) {
        return null;
    }

    @Override
    public Game guess(Player player, String word) {
        return null;
    }

    @Override
    public GameStats concludeGame(Player player) {
        return null;
    }

    @Override
    public GameStatus getGameStatus(Player player) {
        return null;
    }

    @Override
    public void setGameStatus(Player player, GameStatus gameStatus) {

    }

    @Override
    public boolean playersSynced() {
        return false;
    }

    @Override
    public GameRound getCurrentGameRound(Player player) {
        return null;
    }

    @Override
    public GameRound[] getCurrentOpponentGameRounds(Player player) {
        return null;
    }
}