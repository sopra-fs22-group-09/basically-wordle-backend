package ch.uzh.sopra.fs22.backend.wordlepvp;

import ch.uzh.sopra.fs22.backend.wordlepvp.redis.DataRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.redis.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
@Controller
public class SampleController {

    private final DataRepository repository;

    public SampleController(DataRepository dataRepository) {
        this.repository = dataRepository;
    }

    @MutationMapping
    public Lobby addLobby(@Argument @Valid LobbyInput input) {
        return this.repository.saveLobby(input);
    }

    @QueryMapping
    public Lobby lobbyById(@Argument @NotNull Long id) { return this.repository.getLobbyById(id); }

    @QueryMapping
    public String greeting() {
        return this.repository.getBasic();
    }

    @QueryMapping
    public Mono<String> greetingMono() {
        return this.repository.getGreeting();
    }

    @QueryMapping
    public Flux<String> greetingsFlux() {
        return this.repository.getGreetings();
    }

    @SubscriptionMapping
    public Flux<String> greetings() {
        return this.repository.getGreetingsStream();
    }

//    @SubscriptionMapping
//    public Flux<Lobby> lobbyFlux(int lobbyId, GameSettings toBecomeGameInstance) {
//        return this.repository.getLobbies()
//    }

}
