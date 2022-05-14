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
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
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

    public Mono<Lobby> initializeLobby(LobbyInput input/*, Mono<Player> player*/) {

        if (input.getSize() > input.getGameCategory().getMaxGameSize()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lobby size is too big.");
        }

        Lobby lobby = Lobby.builder()
                .id(UUID.randomUUID().toString())
                .name(input.getName())
                .size(input.getSize())
                //.owner(p)
                .status(LobbyStatus.OPEN)
                .gameCategory(input.getGameCategory())
                .gameMode(input.getGameCategory().getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        lobby.setGame(this.createGame(lobby.getId(), lobby.getGameMode()));

        return this.lobbyRepository.saveLobby(lobby).log();

/*        return player.publishOn(Schedulers.boundedElastic()).map(p -> {
            Lobby lobby = Lobby.builder()
                    .id(UUID.randomUUID().toString())
                    .name(input.getName())
                    .size(input.getSize())
                    //.owner(p)
                    .status(LobbyStatus.OPEN)
                    .gameCategory(input.getGameCategory())
                    .gameMode(input.getGameCategory().getDefaultGameMode())
                    .players(new HashSet<>())
                    .build();
            lobby.setGame(this.createGame(lobby.getId(), lobby.getGameMode()));
            return lobby;
        })
                .flatMap(this.lobbyRepository::saveLobby)
                .log();*/

    }

    //TODO: should not be able to be in multiple lobbies !!!
    public Mono<Lobby> addPlayerToLobby(String id, Mono<Player> player) {

        return this.lobbyRepository.getLobby(id)
                .zipWith(player, (l, p) -> {
                    if (l.getStatus().equals(LobbyStatus.FULL)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lobby is already full.");
                    }
                    if (l.getStatus().equals(LobbyStatus.INGAME)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lobby is already in game.");
                    }
                    if (l.getPlayers().size() == 0) {
                        l.setOwner(p);
                    }
                    l.getPlayers().add(p);
                    l.setTimeout(3600L);

                    if (l.getPlayers().size() >= l.getSize()) {
                        l.setStatus(LobbyStatus.FULL);
                    }
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
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "GameMode is not supported by current GameCategory!");
                    }
                    return l;
                })
                .publishOn(Schedulers.boundedElastic()) //TODO NEEDED??
                .mapNotNull(l -> {
                    if (l.getGameMode() != input.getGameMode()) {
                        l.setGameMode(input.getGameMode());
                        l.setGame(this.createGame(l.getId(), l.getGameMode()));
                    } else {
                        if (input.getAmountRounds() > l.getGame().getMaxRounds()) {
                            input.setAmountRounds(l.getGame().getMaxRounds());
                        }
                        if (input.getRoundTime() > l.getGame().getMaxTime()) {
                            input.setRoundTime(l.getGame().getMaxTime());
                        }

                        l.getGame().setAmountRounds(input.getAmountRounds());
                        l.getGame().setRoundTime(input.getRoundTime());
                    }
                    return l;
                })
                .flatMap(this.lobbyRepository::saveLobby)
                .log();

    }

    public Flux<Lobby> subscribeLobby(String id, Mono<Player> player) {
        return this.lobbyRepository.getLobbyStream(id)
                .publishOn(Schedulers.boundedElastic())
                .doFinally(s -> this.lobbyRepository.getAllLobbies()
                        .publishOn(Schedulers.boundedElastic())
                        .zipWith(player, (l, p) -> {
                            l.getPlayers().remove(p);
                            if (l.getStatus().equals(LobbyStatus.FULL)) {
                                l.setStatus(LobbyStatus.OPEN);
                            }

                            if (Objects.equals(l.getOwner().getId(), p.getId())
                                    && l.getPlayers().stream().findFirst().isPresent()) {
                                l.setOwner(l.getPlayers().stream().findFirst().get());
                            }

                            this.lobbyRepository.saveLobby(l).subscribe();
                            if (l.getPlayers().isEmpty()) {
                                //l.setTimeout(10L);
                                this.lobbyRepository.deleteLobby(l.getId())
                                        .filter(lobby -> l.getPlayers().isEmpty())
                                        .delaySubscription(Duration.ofSeconds(10L)).subscribe();
                            }

                            return l;
                        }).subscribe()
                )
                .log();

    }

    public Flux<Lobby> getLobbies() {
        return this.lobbyRepository.getAllLobbies();
    }

    public Flux<List<Lobby>> subscribeLobbies() {
        return this.lobbyRepository.getAllLobbiesStream();
    }

    //TODO MAYBE WE CAN NOW PUT THAT INTO THE GAMESERVICE SINCE LOBBYSERVICE IS NEW
    public Game createGame(String lobbyId, GameMode gameMode) {
        try {
            Class<? extends Game> gameClass = Class.forName("ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes." + gameMode.getClassName()).asSubclass(Game.class);
            Game game = gameClass.getDeclaredConstructor().newInstance();
            game.setId(lobbyId);
            return game;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find Game.");
        }
    }
}
