package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameStatus;
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
                .flatMap(g -> this.reactiveGameRedisTemplate.convertAndSend("game/" + g.getId(), g).thenReturn(g))
                .flatMapIterable(Game::getPlayers)
                .all(p -> game.getGameStatus(p) == GameStatus.GUESSING)
                .flatMap(p -> p ? Mono.defer(() -> this.broadcastGameStatus(game, GameStatus.GUESSING)) : Mono.empty())
                .thenReturn(game)
                .flatMapIterable(Game::getPlayers)
                .filter(p -> game.getGameStatus(p) != GameStatus.GUESSING)
                .flatMap(p -> Mono.defer(() -> this.broadcastGameStatus(game, game.getGameStatus(p))))
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

    public Flux<GameStatus> getGameStatusStream(String pId) {
        return this.reactiveGameStatusRedisTemplate.listenToChannel("gameSync/" + pId)
                .map(ReactiveSubscription.Message::getMessage)
                .log();
    }

    public Flux<Game> getGameStream(String id) {
        return this.reactiveGameRedisTemplate.listenToChannel("game/" + id)
                .map(ReactiveSubscription.Message::getMessage)
                .log();
    }

    public Mono<Long> broadcastGameStatusSingle(String playerId, GameStatus status) {
        return this.reactiveGameStatusRedisTemplate.convertAndSend("gameSync/player/" + playerId, status);
    }

    private Mono<Long> broadcastGameStatus(Game game, GameStatus status) {
        return this.reactiveGameStatusRedisTemplate.convertAndSend("gameSync/game/" + game.getId(), status);
    }
}