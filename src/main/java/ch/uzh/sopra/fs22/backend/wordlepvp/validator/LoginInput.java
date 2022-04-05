package ch.uzh.sopra.fs22.backend.wordlepvp.validator;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class LoginInput {
    @NotNull
    private String username;

    @NotNull
    private String password;
}
