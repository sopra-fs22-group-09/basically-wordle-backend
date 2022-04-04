package ch.uzh.sopra.fs22.backend.wordlepvp;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.redis.DataRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.redis.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.RegisterInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
@Controller
public class SampleController {

    private final UserService userService;

    private final DataRepository dataRepository;

    public SampleController(UserService userService, DataRepository dataRepository) {
        this.userService = userService;
        this.dataRepository = dataRepository;
    }

    @MutationMapping
    public User register(@Argument @Valid RegisterInput input) {
        return this.userService.createUser(input);
    }

    @QueryMapping
    public Lobby lobbyById(@Argument @NotNull Long id) {
        return this.dataRepository.getLobbyById(id);
    }

    @MutationMapping
    public Lobby addLobby(@Argument @Valid LobbyInput input) {
        return this.dataRepository.saveLobby(input);
    }

    @QueryMapping
    public Flux<String> greetingsFlux() {
        return this.dataRepository.getGreetings();
    }

    @SubscriptionMapping
    public Flux<String> greetings() {
        return this.dataRepository.getGreetingsStream();
    }

//    @SubscriptionMapping
//    public Flux<Lobby> lobbyFlux(int lobbyId, GameSettings toBecomeGameInstance) {
//        return this.repository.getLobbies()
//    }

}
