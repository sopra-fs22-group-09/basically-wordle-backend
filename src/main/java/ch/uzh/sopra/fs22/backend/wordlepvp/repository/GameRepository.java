package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStatus;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class GameRepository {

    private final ReactiveRedisTemplate<String, Game> reactiveGameRedisTemplate;

    public GameRepository(ReactiveRedisTemplate<String, Game> reactiveGameRedisTemplate) {
        this.reactiveGameRedisTemplate = reactiveGameRedisTemplate;
    }

    public Mono<Game> saveGame(Game game) {
        return this.reactiveGameRedisTemplate.<String, Game>opsForHash()
                .put("games", game.getId(), game)
                .map(g -> game)
                .flatMap(g -> this.reactiveGameRedisTemplate.convertAndSend("game/" + g.getId(), g))
                .then(Mono.just(game))
                .log();

    }

    public void deleteGame(String id) {
        this.reactiveGameRedisTemplate.<String, Game>opsForHash()
                .remove("games", id)
                //.publishOn(Schedulers.boundedElastic()) //needed?
                .subscribe();

    }

    public Mono<Game> getGame(String id) {
        return this.reactiveGameRedisTemplate.<String, Game>opsForHash()
                .get("games", id)
                .log();

    }

    public Flux<Game> getGameStream(String id) {
        return this.reactiveGameRedisTemplate.listenToChannel("game/" + id)
                .map(ReactiveSubscription.Message::getMessage)
                .log();
    }

}