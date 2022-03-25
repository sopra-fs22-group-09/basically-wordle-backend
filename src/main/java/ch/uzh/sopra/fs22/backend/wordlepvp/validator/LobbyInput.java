package ch.uzh.sopra.fs22.backend.wordlepvp.validator;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class LobbyInput {

    @NotNull
    private Long id;

    @NotNull
    @Length(min = 3, max = 50)
    private String name;
}
