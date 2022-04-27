package ch.uzh.sopra.fs22.backend.wordlepvp.model;

public enum GameCategory {

    PVP(GameMode.WORDSPP, 6),
    SOLO(GameMode.CLASSIC, 1),
    COOP(GameMode.ONEWORD, 4);

    private final GameMode defaultGameMode;
    private final int maxGameSize;

    GameCategory(GameMode defaultGameMode, int maxGameSize) {
        this.defaultGameMode = defaultGameMode;
        this.maxGameSize = maxGameSize;
    }

    public GameMode getDefaultGameMode() {
        return defaultGameMode;
    }

    public int getMaxGameSize() {
        return maxGameSize;
    }
}
