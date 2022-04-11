package ch.uzh.sopra.fs22.backend.wordlepvp.validator;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class ResetInput {

    @NotNull
    @Email
    private String email;
}
