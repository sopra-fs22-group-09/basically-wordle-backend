package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import ch.uzh.sopra.fs22.backend.wordlepvp.util.AuthorizationHelper;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Validated
@Controller
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @MutationMapping
    public User register(@Argument @Valid RegisterInput input) {
        return this.userService.createUser(input);
    }

    @MutationMapping
    public User login(@Argument @Valid LoginInput input) {
        return this.userService.validateUser(input);
    }

    @MutationMapping
    public boolean logout(@ContextValue(name = "Authorization") String authHeader) {
        return this.userService.logout(AuthorizationHelper.extractAuthToken(authHeader));
    }

    @MutationMapping
    public void reset(@Argument @Valid ResetInput input) { // TODO why does a mutation return void?
        this.userService.resetPassword(input);
    }

    @MutationMapping
    public void resetWithToken(@Argument @Valid ResetTokenInput input) { // TODO why does a mutation return void?
        this.userService.resetWithToken(input);
    }

    @MutationMapping
    public boolean tutorialFinished(@ContextValue(name = "Authorization") String authHeader) {
        return this.userService.completeTutorial(AuthorizationHelper.extractAuthToken(authHeader));}
}
