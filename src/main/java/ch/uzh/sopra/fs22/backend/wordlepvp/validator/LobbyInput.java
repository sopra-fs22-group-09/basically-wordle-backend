package ch.uzh.sopra.fs22.backend.wordlepvp.validator;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameCategory;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class LobbyInput {

    @NotNull
    @Length(min = 3, max = 50)
    private String name;

    @NotNull
    private Integer size;

    @NotNull
    private GameCategory gameCategory;
}
