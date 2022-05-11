package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.UserStatus;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import ch.uzh.sopra.fs22.backend.wordlepvp.util.AuthorizationHelper;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LoginInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.RegisterInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetTokenInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

@Validated
@Controller
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public List<User> friendsByStatus(@Argument @Valid UserStatus status, @ContextValue(name = "Authorization") String authHeader) {
        User user = this.userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.userService.friends(status, user);
    }

    @QueryMapping
    public List<User> allFriends(@ContextValue(name = "Authorization") String authHeader) {
        User user = this.userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.userService.friends(user);
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
    public boolean addFriend(@Argument @Valid String friendId, @ContextValue(name = "Authorization") String authHeader) {
        User user = this.userService.getFromToken(AuthorizationHelper.extractAuthToken(authHeader));
        return this.userService.addFriend(friendId, user);
    }

    @MutationMapping
    public boolean tutorialFinished(@ContextValue(name = "Authorization") String authHeader) {
        return this.userService.completeTutorial(AuthorizationHelper.extractAuthToken(authHeader));
    }

//    @SubscriptionMapping
//    public Flux<FriendInvite> friendRequests(@ContextValue(name = "Authorization") String authHeader) {
//        return this.userService.receiveFriendRequests(AuthorizationHelper.extractAuthToken(authHeader));
//    }
}
