package ch.uzh.sopra.fs22.backend.wordlepvp.validator;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ResetInput {

    @NotNull
    @Email
    private String email;
}