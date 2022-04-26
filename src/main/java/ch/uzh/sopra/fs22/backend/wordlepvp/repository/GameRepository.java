package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GameRepository {

    private final ReactiveRedisTemplate<String, Game> reactiveRedisTemplate;

    public GameRepository(ReactiveRedisTemplate<String, Game> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Game> saveGame(String id, Game game) {
        return this.reactiveRedisTemplate.<String, Game>opsForHash()
                .put("games", id, game)
                .map(g -> game)
                .log();
    }

    public Mono<Game> getGame(String id) {
        return this.reactiveRedisTemplate.<String, Game>opsForHash()
                .get("games", id)
                .log();
    }

/*    public Mono<Game> getGameByPlayer(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(lId -> this.reactiveRedisTemplate.<String, Lobby>opsForHash()
                        .get("lobbies", lId)
                        .map(Lobby::getGame))
                .log();
    }*/

/*    public Mono<Game> updateGameByPlayer(Mono<Player> player, Game game) {

        return player.map(Player::getLobbyId)
                .flatMap(lobbyId -> this.reactiveRedisTemplate.<String, Lobby>opsForHash()
                        .get("lobbies", lobbyId)
                        .log())
                .mapNotNull(lobby -> {
                    lobby.setGame(game);
                    return lobby;
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(lobby -> this.reactiveRedisTemplate.<String, Lobby>opsForHash()
                        .put("lobbies", lobby.getId(), lobby).subscribe())
                .map(Lobby::getGame)
                .log();
    }*/
}