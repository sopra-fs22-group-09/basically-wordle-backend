package ch.uzh.sopra.fs22.backend.wordlepvp;

import ch.uzh.sopra.fs22.backend.wordlepvp.DataRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class SampleController {

    private final DataRepository repository;

    public SampleController(DataRepository dataRepository) {
        this.repository = dataRepository;
    }

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
