package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GameRepository {

    private final ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate;

    public GameRepository(ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    //TODO @COMPILOMATIC : PROB MOVE GAMESTART TO SERVICE AND NOT REPO?

    public Mono<Game> initializeGame(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(lId -> this.reactiveRedisTemplate.<String, Lobby>opsForHash()
                        .get("lobbies", lId)
                        .log()
                        .map(Lobby::getGame))
                .doOnNext(Game::start)
                .log();
    }

    public Mono<Game> getGameByPlayer(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(lId -> this.reactiveRedisTemplate.<String, Lobby>opsForHash()
                        .get("lobbies", lId)
                        .map(Lobby::getGame))
                .log();
    }

    public Mono<Game> updateGameByPlayer(Mono<Player> player, Game game) {

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
    }

}