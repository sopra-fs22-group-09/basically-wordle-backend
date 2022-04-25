package ch.uzh.sopra.fs22.backend.wordlepvp.model;

public enum GameMode {

    WORDSPP("WordsPP", "PVP"),
    SONICFAST("SonicFast", "PVP"),
    TIMERESET("TimeReset", "PVP"),
    PARTY("Party", "PVP"),
    CHALLENGE("Challenge", "PVP"),
    CHAIN("Chain", "PVP"),
    CLASSIC("Classic", "SOLO"),
    INTIME("InTime", "SOLO"),
    PLAYERVSAI("PlayerVsAI", "SOLO"),
    ONEWORD("OneWord", "COOP"),
    WORDCOMBINATION("WordCombination", "COOP");

    private final String className;
    private final String category;

    GameMode(String className, String category) {
        this.className = className;
        this.category = category;
    }

    public String getClassName() {
        return className;
    }

    public String getCategory() {
        return category;
    }
}