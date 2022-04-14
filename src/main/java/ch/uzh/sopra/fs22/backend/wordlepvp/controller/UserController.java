package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import ch.uzh.sopra.fs22.backend.wordlepvp.util.AuthorizationHelper;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LoginInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.RegisterInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetTokenInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.util.context.Context;

import javax.validation.Valid;

@Validated
@Controller
public class UserController {

    private final UserService userService;

    private static final String CONTEXT_VIEW = "org.springframework.graphql.execution.ReactorContextManager.CONTEXT_VIEW";

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
    public boolean logout(@ContextValue(name = CONTEXT_VIEW) Context ctx) {
        String token = AuthorizationHelper.getAuthTokenFromContext(ctx);
        return this.userService.logout(token);
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
