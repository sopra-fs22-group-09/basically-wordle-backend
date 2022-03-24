package ch.uzh.sopra.fs22.backend.wordlepvp;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class DataRepository {

    public String getBasic() {
        return "Hello world!";
    }

    public Mono<String> getGreeting() {
        return Mono.delay(Duration.ofMillis(500)).map(aLong -> "Hello!");
    }

    public Flux<String> getGreetings() {
        return Mono.delay(Duration.ofMillis(500))
                .flatMapMany(aLong -> Flux.just("Hi!", "Bonjour!", "Hola!", "Ciao!", "Zdravo!"));
    }

    public Flux<String> getGreetingsStream() {
        return Mono.delay(Duration.ofMillis(2000))
                .flatMapMany(aLong -> Flux
                .just("Hi!", "Bonjour!", "Hola!", "Ciao!", "Zdravo!")
                .log()
                .delayElements(Duration.ofSeconds(2)));
    }

}
