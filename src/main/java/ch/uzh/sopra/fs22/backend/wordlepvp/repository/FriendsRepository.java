package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FriendsRepository {
    private final ReactiveRedisTemplate<String, User> reactiveRedisTemplate;

    public FriendsRepository(ReactiveRedisTemplate<String, User> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Flux<User> getFriendsStream(User user) {
        return this.reactiveRedisTemplate.listenToChannel("friends")
                .map(ReactiveSubscription.Message::getMessage)
                .filter(receivedFriend -> user.getFriends().stream().anyMatch(f -> f.getId().equals(receivedFriend.getId())))
                .distinctUntilChanged();

    }

    public Mono<Void> broadcastFriendsEvent(User friend) {
        return this.reactiveRedisTemplate.convertAndSend("friends", friend).then();
    }
}
