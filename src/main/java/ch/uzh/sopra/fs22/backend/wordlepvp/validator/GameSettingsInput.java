package ch.uzh.sopra.fs22.backend.wordlepvp.validator;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameMode;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GameSettingsInput {

    @NotNull
    private GameMode gameMode;

    private Integer amountRounds;

    private Integer roundTime;
}
