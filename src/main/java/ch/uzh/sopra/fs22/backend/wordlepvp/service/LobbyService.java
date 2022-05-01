package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.GameSettingsInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class LobbyService {

    private final LobbyRepository lobbyRepository;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    public Mono<Lobby> initializeLobby(LobbyInput input, Mono<Player> player) {

        if (input.getSize() > input.getGameCategory().getMaxGameSize()) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Lobby size is too big.");
        }

        return player.publishOn(Schedulers.boundedElastic()).map(p -> {
            Lobby lobby = Lobby.builder()
                    .id(UUID.randomUUID().toString())
                    .name(input.getName())
                    .size(input.getSize())
                    .owner(p)
                    .status(LobbyStatus.OPEN)
                    .gameCategory(input.getGameCategory())
                    .gameMode(input.getGameCategory().getDefaultGameMode())
                    .players(new HashSet<>())
                    .build();
            lobby.setGame(this.createGame(lobby.getId(), lobby.getGameMode()));
            return lobby;
        })
                .flatMap(this.lobbyRepository::saveLobby)
                .log();

    }

    public Mono<Lobby> addPlayerToLobby(String id, Mono<Player> player) {

        return this.lobbyRepository.getLobby(id)
                .zipWith(player, (l, p) -> {
                    l.getPlayers().add(p);
                    return l;
                })
                .flatMap(this.lobbyRepository::saveLobby)
                .log();

    }

    public Mono<Lobby> changeLobby(GameSettingsInput input, Mono<Player> player) {

        return player.mapNotNull(Player::getLobbyId)
                .flatMap(this.lobbyRepository::getLobby)
                .zipWith(player, (l, p) -> {
                    if (!Objects.equals(l.getOwner().getId(), p.getId())) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only Lobby owner is allowed to change settings!");
                    }
                    if (GameCategory.valueOf(input.getGameMode().getCategory()) != l.getGameCategory()) {
                        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "GameMode is not supported by current GameCategory!");
                    }
                    return l;
                })
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(l -> {
                    if (l.getGameMode() != input.getGameMode()) {
                        l.setGameMode(input.getGameMode());
                        l.setGame(this.createGame(l.getId(), l.getGameMode()));
                    } else {
                        l.getGame().setAmountRounds(input.getAmountRounds());
                        l.getGame().setRoundTime(input.getRoundTime());
                    }
                    return l;
                })
                .flatMap(this.lobbyRepository::saveLobby)
                .log();

    }

    public Flux<Lobby> subscribeLobby(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMapMany(this.lobbyRepository::getLobbyStream)
                .publishOn(Schedulers.boundedElastic())
                .doFinally(s -> this.lobbyRepository.getAllLobbies()
                        .publishOn(Schedulers.boundedElastic())
                        .zipWith(player, (l, p) -> {
                            l.getPlayers().remove(p);

                            if (Objects.equals(l.getOwner().getId(), p.getId())
                                    && l.getPlayers().stream().findFirst().isPresent()) {
                                l.setOwner(l.getPlayers().stream().findFirst().get());
                            }

                            if (!l.getPlayers().isEmpty()) {
                                this.lobbyRepository.saveLobby(l).subscribe();
                            } else {
                                this.lobbyRepository.deleteLobby(l.getId()).subscribe();
                            }
                            return l;
                        }).subscribe()
                )
                .log();

    }

    public Flux<Lobby> getLobbies() {
        return this.lobbyRepository.getAllLobbies();
    }

    //TODO MAYBE WE CAN NOW PUT THAT INTO THE GAMESERVICE SINCE LOBBYSERVICE IS NEW
    public Game createGame(String lobbyId, GameMode gameMode) {
        try {
            Class<? extends Game> gameClass = Class.forName("ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes." + gameMode.getClassName()).asSubclass(Game.class);
            Game game = gameClass.getDeclaredConstructor().newInstance();
            game.setId(lobbyId);
            game.setStatus(GameStatus.NEW);
            return game;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find Game.");
        }
    }
}
