package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.logic.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStatus;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GameRepository {

    private final ReactiveRedisTemplate<String, Game> reactiveGameRedisTemplate;
    private final ReactiveRedisTemplate<String, GameStatus> reactiveGameStatusRedisTemplate;

    public GameRepository(ReactiveRedisTemplate<String, Game> reactiveGameRedisTemplate, ReactiveRedisTemplate<String, GameStatus> reactiveGameStatusRedisTemplate) {
        this.reactiveGameRedisTemplate = reactiveGameRedisTemplate;
        this.reactiveGameStatusRedisTemplate = reactiveGameStatusRedisTemplate;
    }

    public Mono<Game> saveGame(Game game) {
        return this.reactiveGameRedisTemplate.<String, Game>opsForHash()
                .put("games", game.getId(), game)
                .map(g -> game)
                .flatMap(g -> this.broadcastGame(g).thenReturn(g))
                .flatMapIterable(Game::getPlayers)
                .flatMap(p -> this.broadcastGameStatusSingle(p.getId(), game.getGameStatus(p)).thenReturn(p))
                .then(Mono.just(game))
                .log();
    }

    public Mono<Long> deleteGame(String id) {
        return this.reactiveGameRedisTemplate.<String, Game>opsForHash()
                .remove("games", id);
    }

    public Mono<Game> getGame(String id) {
        return this.reactiveGameRedisTemplate.<String, Game>opsForHash()
                .get("games", id)
                .log();
    }

    public Flux<GameStatus> getGameStatusStream(String id, Player player) {
        return this.reactiveGameStatusRedisTemplate.listenToChannel("gameSync/game/" + id, "gameSync/player/" + player.getId())
                .map(ReactiveSubscription.Message::getMessage)
                .distinctUntilChanged()
                .log();
    }

    public Flux<Game> getGameStream(String id) {
        return this.reactiveGameRedisTemplate.listenToChannel("game/" + id)
                .map(ReactiveSubscription.Message::getMessage)
                .log();
    }

    public Mono<Long> broadcastGame(Game game) {
        return this.reactiveGameRedisTemplate.convertAndSend("game/" + game.getId(), game);
    }

    public Mono<Long> broadcastGameStatusSingle(String playerId, GameStatus status) {
        return this.reactiveGameStatusRedisTemplate.convertAndSend("gameSync/player/" + playerId, status);
    }

    public Mono<Long> broadcastGameStatus(Game game, GameStatus status) {
        return this.reactiveGameStatusRedisTemplate.convertAndSend("gameSync/game/" + game.getId(), status);
    }
}