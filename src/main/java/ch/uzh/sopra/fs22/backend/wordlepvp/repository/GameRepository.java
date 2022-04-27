package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
                .log();

    }

    public void deleteGame(String id) {
        this.reactiveRedisTemplate.<String, Game>opsForHash()
                .remove("games", id)
                .subscribe();

    }

    public Mono<Game> getGame(String id) {
        return this.reactiveRedisTemplate.<String, Game>opsForHash()
                .get("games", id)
                .log();

    }
}