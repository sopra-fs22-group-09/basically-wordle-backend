package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PlayerRepository {

    ReactiveRedisTemplate<String, ?> reactiveRedisTemplate;

    public PlayerRepository(ReactiveRedisTemplate<String, ?> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }


}
