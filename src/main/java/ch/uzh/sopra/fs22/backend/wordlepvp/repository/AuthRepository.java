package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Component
public class AuthRepository {

    private final RedisTemplate<String, UUID> redisTemplate;
    private final Duration logoutAfter = Duration.ofHours(8);

    public AuthRepository(RedisTemplate<String, UUID> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String setAuthToken(User user) {
        String authToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(authToken, user.getId(), logoutAfter);
        return authToken;
    }

    public boolean isAuthorized(String token) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfPresent(token, Objects.requireNonNull(redisTemplate.opsForValue().get(token)), logoutAfter));
    }

    public boolean isPretendUser (User user, String token) {
        UUID uuidFromToken = redisTemplate.opsForValue().get(token);
        return user.getId().equals(uuidFromToken) && Boolean.TRUE.equals(redisTemplate.opsForValue().setIfPresent(token, uuidFromToken, logoutAfter));
    }

    public UUID getUserID(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public void expire(String token) {
        redisTemplate.delete(token);
    }
}
