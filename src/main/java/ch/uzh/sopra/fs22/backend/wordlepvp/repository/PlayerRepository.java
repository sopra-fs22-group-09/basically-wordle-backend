package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PlayerRepository {

    private final ReactiveRedisTemplate<String, Player> reactiveRedisTemplate;

    public PlayerRepository(ReactiveRedisTemplate<String, Player> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Player> savePlayer(Player player) {
        return this.reactiveRedisTemplate.<String, Player>opsForHash()
                .put("players", player.getId(), player)
                .map(p -> player)
                .log();
    }

    public Mono<Player> findById(String id) {
        return this.reactiveRedisTemplate.<String, Player>opsForHash().get("players", id);
    }
}
