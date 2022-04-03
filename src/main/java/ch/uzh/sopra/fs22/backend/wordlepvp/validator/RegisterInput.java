package ch.uzh.sopra.fs22.backend.wordlepvp.validator;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class RegisterInput {
    @NotNull
    private String username;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Length(min = 5, max = 50)
    private String password;
}
