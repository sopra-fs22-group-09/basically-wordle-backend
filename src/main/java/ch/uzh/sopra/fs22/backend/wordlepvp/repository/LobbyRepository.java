package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.GameService;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.GameSettingsInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
public class LobbyRepository {

    private final ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate;

    public LobbyRepository(ReactiveRedisTemplate<String, Lobby> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    // TODO: Check for lobby name duplicates ? maybe. or maybe not. or no, not at all.
    // TODO: handle 2 browser sessions, change status on full & allow entry if not full

    public Mono<Lobby> saveLobby(LobbyInput input, User player) {

        if (input.getSize() > input.getGameCategory().getMaxGameSize()) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Lobby size is too big.");
        }

        Lobby lobby = Lobby.builder()
                .id(UUID.randomUUID().toString())
                .name(input.getName())
                .size(input.getSize())
                .owner(player)
                .status(LobbyStatus.OPEN)
                .gameCategory(input.getGameCategory())
                .gameMode(input.getGameCategory().getDefaultGameMode())
                .players(new HashSet<>())
                .build();

        lobby.setGame(this.createGame(lobby.getGameMode()));

        return this.reactiveRedisTemplate.opsForHash()
                .put("lobbies", lobby.getId(), lobby)
                .map(l -> lobby)
                .log();
    }

    public Mono<Lobby> playerJoinLobby(String id, User player) {

        return this.reactiveRedisTemplate.<String, Lobby>opsForHash().get("lobbies", id)
                .mapNotNull(l -> {
                    l.getPlayers().add(player);
                    return l;
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(l -> this.reactiveRedisTemplate.<String, Lobby>opsForHash().put("lobbies", l.getId(), l).subscribe())
                .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobbyplayers/" + l.getId(), l).subscribe());
    }

    public Mono<Lobby> changeLobby(String id, GameSettingsInput gameSettings, User player) {

        return this.reactiveRedisTemplate.<String, Lobby>opsForHash().get("lobbies", id)
                .doOnNext(l -> {
                    if (!Objects.equals(l.getOwner().getId(), player.getId())) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only Lobby owner is allowed to change settings!");
                    }
                    if (GameCategory.valueOf(gameSettings.getGameMode().getCategory()) != l.getGameCategory()) {
                        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "GameMode is not supported by current GameCategory!");
                    }
                })
                .mapNotNull(l -> {
                    if (l.getGameMode() != gameSettings.getGameMode()) {
                        l.setGameMode(gameSettings.getGameMode());
                        l.setGame(this.createGame(l.getGameMode()));
                    } else {
                        l.getGame().setAmountRounds(gameSettings.getAmountRounds());
                        l.getGame().setRoundTime(gameSettings.getRoundTime());
                    }
                    return l;
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(l -> this.reactiveRedisTemplate.<String, Lobby>opsForHash().put("lobbies", l.getId(), l).subscribe())
                .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobbysettings/" + l.getId(), l).subscribe());
    }

    public Flux<Lobby> getLobbyStream(String id, User player) {

        return this.reactiveRedisTemplate
                // TODO: Replace with pattern syntax 'lobby*'
                .listenToChannel("lobbyplayers/" + id, "lobbysettings/" + id)
                .map(ReactiveSubscription.Message::getMessage)
                .log()
                .publishOn(Schedulers.boundedElastic())
                .doFinally(s -> this.reactiveRedisTemplate.<String, Lobby>opsForHash().values("lobbies")
                        .filter(l -> l.getPlayers().contains(player))
                        .publishOn(Schedulers.boundedElastic())
                        .doOnNext(l -> {
                            l.getPlayers().remove(player);
                            if (!l.getPlayers().contains(l.getOwner()) && l.getPlayers().stream().findFirst().isPresent()) {
                                l.setOwner(l.getPlayers().stream().findFirst().get());
                            }
                            this.reactiveRedisTemplate.<String, Lobby>opsForHash().put("lobbies", l.getId(), l).subscribe();
                        })
                        .doOnNext(l -> {
                            if (l.getPlayers().isEmpty()) {
                                this.reactiveRedisTemplate.<String, Lobby>opsForHash().remove("lobbies", l.getId()).subscribe();
                            }
                        })
                        .doOnNext(l -> this.reactiveRedisTemplate.convertAndSend("lobbyplayers/" + l.getId(), l).subscribe())
                        .subscribe());
    }

    public Flux<Lobby> getAllLobbies() {
        return this.reactiveRedisTemplate.<String, Lobby>opsForHash().values("lobbies");
    }

    public Mono<Game> getGameByLobbyId(String id) {
        return this.reactiveRedisTemplate.<String, Lobby>opsForHash().get("lobbies", id)
                .map(Lobby::getGame);
    }

    public Game createGame(GameMode gameMode) {
        try {
            Class<? extends Game> gameClass = Class.forName("ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes." + gameMode.getClassName()).asSubclass(Game.class);
            return gameClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find Game.");
        }
    }
}