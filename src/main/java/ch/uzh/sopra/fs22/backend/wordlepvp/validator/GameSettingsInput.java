package ch.uzh.sopra.fs22.backend.wordlepvp.validator;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameMode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GameSettingsInput {

    @NotNull
    private GameMode gameMode;

    private Integer amountRounds;

    private Integer roundTime;

    private String[] categories;
}
