package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.PlayerRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.GameSettingsInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import graphql.GraphQLException;
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
    private final PlayerRepository playerRepository;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository, PlayerRepository playerRepository) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
    }

    public Mono<Lobby> getLobbyById(String lobbyId) {
        return this.lobbyRepository.getLobby(lobbyId);
    }

    public Mono<Lobby> initializeLobby(LobbyInput input, Mono<Player> player) {

        if (input.getSize() > input.getGameCategory().getMaxGameSize()) {
            throw new GraphQLException("Lobby size is too big.");
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

    //TODO: should not be able to be in multiple lobbies !!!
    public Mono<Lobby> addPlayerToLobby(String id, Mono<Player> player) {

        return this.lobbyRepository.getLobby(id)
                .zipWith(player, (l, p) -> {
                    if (l.getStatus().equals(LobbyStatus.INGAME)) {
                        throw new GraphQLException("Cannot join a lobby that is in game!");
                    }
                    if (l.getStatus().equals(LobbyStatus.FULL)) {
                        throw new GraphQLException("Cannot join a full Lobby!");
                    }
                    p.setLobbyId(id);
                    this.playerRepository.savePlayer(p).subscribe(); //Chamer de subscribe echt irgendwie ersetze?
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
                        throw new GraphQLException("Only Lobby owner is allowed to change settings!");
                    }
                    if (GameCategory.valueOf(input.getGameMode().getCategory()) != l.getGameCategory()) {
                        throw new GraphQLException("GameMode is not supported by current GameCategory!");
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
                    }
                    return l;
                })
                .flatMap(this.lobbyRepository::saveLobby)
                .log();

    }

    // TODO: Check whether lobby full and reject
    public Flux<Lobby> subscribeLobby(String id, Mono<Player> player) {
        return this.lobbyRepository.getLobbyStream(id)
                //.filter(l -> l.getStatus() == LobbyStatus.OPEN) //@elvio das gaht nid, all andere subs werded au gfiltered
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

                            if (l.getPlayers().isEmpty()) {
                                //l.setTimeout(10L);
                                this.lobbyRepository.saveLobby(l).subscribe();
                                this.lobbyRepository.deleteLobby(l.getId())
                                        .delaySubscription(Duration.ofSeconds(5L))
                                        .zipWith(this.lobbyRepository.getLobby(id))
                                        .filter(ll -> ll.getT2().getPlayers().isEmpty())
                                        .subscribe();
                            } else {
                                this.lobbyRepository.saveLobby(l).subscribe();
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
            throw new GraphQLException("Could not find Game.");
        }
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
}
