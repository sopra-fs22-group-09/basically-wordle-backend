package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LoginInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.RegisterInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetTokenInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Validated
@Controller
public class SampleController {

    private final UserService userService;

    public SampleController(UserService userService) {
        this.userService = userService;
    }

    @MutationMapping
    public User register(@Argument @Valid RegisterInput input) {
        //TODO: add header
        return this.userService.createUser(input);
    }

    @MutationMapping
    public User login(@Argument @Valid LoginInput input) {
        //TODO: add header
        return this.userService.validateUser(input);
    }

    @MutationMapping
    public void reset(@Argument @Valid ResetInput input) {
        this.userService.resetPassword(input);
    }

    @MutationMapping
    public void resetWithToken(@Argument @Valid ResetTokenInput input) {
        this.userService.resetWithToken(input);
    }
}
