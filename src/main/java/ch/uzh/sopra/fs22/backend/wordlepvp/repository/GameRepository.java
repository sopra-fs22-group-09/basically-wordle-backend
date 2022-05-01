package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class GameRepository {

    private final ReactiveRedisTemplate<String, Game> reactiveRedisTemplate;

    public GameRepository(ReactiveRedisTemplate<String, Game> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Game> saveGame(Game game) {
        return this.reactiveRedisTemplate.<String, Game>opsForHash()
                .put("games", game.getId(), game)
                .map(g -> game)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(g -> this.reactiveRedisTemplate.convertAndSend("game/" + g.getId(), g).subscribe())
                .log();

    }

    public void deleteGame(String id) {
        this.reactiveRedisTemplate.<String, Game>opsForHash()
                .remove("games", id)
                //.publishOn(Schedulers.boundedElastic()) //needed?
                .subscribe();

    }

    public Mono<Game> getGame(String id) {
        return this.reactiveRedisTemplate.<String, Game>opsForHash()
                .get("games", id)
                .log();

    }

    public Flux<Game> getGameStream(String id) {
        return this.reactiveRedisTemplate.listenToChannel("game/" + id)
                .map(ReactiveSubscription.Message::getMessage)
                .log();
    }
}