package ch.uzh.sopra.fs22.backend.wordlepvp.model;

public enum GameCategory {

    PVP(GameMode.WORDSPP),
    SOLO(GameMode.CLASSIC),
    COOP(GameMode.ONEWORD);

    private final GameMode defaultGameMode;

    GameCategory(GameMode defaultGameMode) {
        this.defaultGameMode = defaultGameMode;
    }

    public GameMode getDefaultGameMode() {
        return defaultGameMode;
    }
}
