package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class AuthRepository {

    private RedisTemplate<String, UUID> redisTemplate;

    public AuthRepository(RedisTemplate<String, UUID> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String setAuthToken(User user) {
        String authToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(authToken, user.getId(), Duration.ofSeconds(60));
        return authToken;
    }

    public boolean isAuthorized(String token) {
        return redisTemplate.opsForValue().get(token) != null;
    }

    public boolean isPretendUser (User user, String token) {
        return user.getId().equals(redisTemplate.opsForValue().get(token));
    }

    public UUID getUserID(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public void expire(String token) {
        redisTemplate.delete(token);
    }
}
