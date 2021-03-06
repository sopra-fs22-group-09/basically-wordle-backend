package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.logic.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.PlayerRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.GameSettingsInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;

@Service
@Transactional
public class LobbyService {

    private final LobbyRepository lobbyRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final Map<String, Disposable> lobbyDeletion;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository, GameRepository gameRepository, PlayerRepository playerRepository) {
        this.lobbyRepository = lobbyRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.lobbyDeletion  = new HashMap<>();
    }

    public Mono<Lobby> getLobbyById(String lobbyId) {
        return this.lobbyRepository.hasLobby(lobbyId)
                .doOnNext(b -> {
                    if (!b) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lobby does not exist!");
                    }
                })
                .then(this.lobbyRepository.getLobby(lobbyId));
    }

    public Mono<Lobby> initializeLobby(LobbyInput input, Mono<Player> player) {

        if (input.getSize() > input.getGameCategory().getMaxGameSize()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lobby size is too big.");
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
                    .categories(new ArrayList<>())
                    .players(new HashSet<>())
                    .build();
            lobby.setGame(this.createGame(lobby.getId(), lobby.getGameMode()));
            return lobby;
        })
                .flatMap(this.lobbyRepository::saveLobby)
                .log();

    }

    public Mono<Boolean> reinitializeLobby(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(this.lobbyRepository::getLobby)
                .publishOn(Schedulers.boundedElastic())
                .map(l -> {
                    l.setStatus(l.getPlayers().size() >= l.getSize() ? LobbyStatus.FULL : LobbyStatus.OPEN);
                    l.setGame(this.createGame(l.getId(), l.getGameMode()));
                    return l;
                })
                .flatMap(this.lobbyRepository::saveLobby)
                .flatMap(l -> this.gameRepository.broadcastGameStatus(l.getGame(), GameStatus.NEW).thenReturn(l))
                .then(Mono.just(true))
                .log();

    }
    public Mono<Lobby> addPlayerToLobby(String id, Mono<Player> player) {

        return this.lobbyRepository.getLobby(id)
                .publishOn(Schedulers.boundedElastic())
                .zipWith(player, (l, p) -> {
                    if (l.getStatus().equals(LobbyStatus.INGAME)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot join a lobby that is in game!");
                    }
                    if (l.getStatus().equals(LobbyStatus.FULL) && !l.getPlayers().contains(p)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot join a full lobby!");
                    }
                    p.setLobbyId(id);
                    this.playerRepository.savePlayer(p).subscribe();
                    l.getPlayers().add(p);

                    Map<String, Disposable> lobbyDisposable = this.lobbyDeletion;
                    if (lobbyDisposable.get(id) != null) {
                        lobbyDisposable.get(id).dispose();
                        this.lobbyDeletion.remove(id);
                        l.setOwner(p);
                    }
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
                .publishOn(Schedulers.boundedElastic())
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
                        l.setCategories(Arrays.asList(input.getCategories()));
                    }
                    return l;
                })
                .flatMap(this.lobbyRepository::saveLobby)
                .log();

    }

    public Flux<Lobby> subscribeLobby(String id, Mono<Player> player) {
        return this.lobbyRepository.getLobbyStream(id)
                .publishOn(Schedulers.boundedElastic())
                .doFinally(ignored -> this.lobbyRepository.getLobby(id)
                        .publishOn(Schedulers.boundedElastic())
                        .zipWith(player, (l, p) -> {
                            l.getPlayers().remove(p);

                            if (l.getPlayers().size() < l.getSize() && l.getStatus() != LobbyStatus.INGAME) {
                                l.setStatus(LobbyStatus.OPEN);
                            }

                            if (Objects.equals(l.getOwner().getId(), p.getId())
                                    && l.getPlayers().stream().findFirst().isPresent()) {
                                l.setOwner(l.getPlayers().stream().findFirst().get());
                            }

                            this.lobbyRepository.saveLobby(l).subscribe();
                            if (l.getPlayers().isEmpty()) {
                                this.lobbyDeletion.put(l.getId(),
                                        this.lobbyRepository.deleteLobby(l.getId())
                                        .delaySubscription(Duration.ofSeconds(10L))
                                        .subscribe());
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

    public Flux<LobbyInvite> receiveLobbyInvites(User user) {
        return this.lobbyRepository.getInvitesStream(user);
    }

    public Mono<Boolean> sendLobbyInvite(String lobbyId, User recipient, User sender) {
        LobbyInvite invite = LobbyInvite.builder()
                .id(UUID.randomUUID().toString())
                .lobbyId(lobbyId)
                .senderId(sender.getId().toString())
                .recipientId(recipient.getId().toString())
                .timeout(3600L) // 1h
                .build();
        return this.lobbyRepository.inviteToLobby(invite);
    }

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
