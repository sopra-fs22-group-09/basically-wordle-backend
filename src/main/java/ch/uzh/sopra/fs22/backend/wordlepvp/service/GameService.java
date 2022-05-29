package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.logic.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.logic.GameRound;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.WordsRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.util.GameTimerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final LobbyRepository lobbyRepository;
    private final WordsRepository wordsRepository;
    private final Map<String, Timer> gameTimers;

    @Autowired
    public GameService(GameRepository gameRepository, LobbyRepository lobbyRepository, WordsRepository wordsRepository) {
        this.gameRepository = gameRepository;
        this.lobbyRepository = lobbyRepository;
        this.wordsRepository = wordsRepository;
        this.gameTimers = new HashMap<>();
    }

    public Mono<Game> initializeGame(Mono<Player> player) {

        return player.zipWhen(p -> this.lobbyRepository.getLobby(p.getLobbyId()))
                .doOnNext(pl -> {
                    if (pl.getT2().getPlayers().size() < 2 && pl.getT2().getGameCategory() != GameCategory.SOLO) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot start a multiplayer game alone!");
                    }
                })
                .filter(t -> t.getT2().getGameCategory() == GameCategory.SOLO
                        && t.getT2().getGame().getGameStatus(t.getT1()) != GameStatus.GUESSING)
                .doOnNext(t -> t.getT2().getGame().setGameStatus(t.getT1(), GameStatus.GUESSING))
                .zipWhen(t -> this.gameRepository.saveGame(t.getT2().getGame()), (lp, p) -> lp)
                .switchIfEmpty(player.zipWhen(p -> this.lobbyRepository.getLobby(p.getLobbyId())))
                .doOnNext(t -> t.getT2().setStatus(LobbyStatus.INGAME))
                .zipWhen(t -> this.lobbyRepository.saveLobby(t.getT2()), (lp, p) -> lp)
                .filter(t -> t.getT2().getGame().getGameStatus(t.getT1()) == GameStatus.GUESSING)
                .map(l -> l.getT2().getGame().start(l.getT2().getPlayers(), this.wordsRepository.getWordsByTopics(l.getT2().getCategories().toArray(new String[0]), 500), this.wordsRepository.getAllAllowedWords()))
                .map(g -> {
                    if (g.getMaxTime() == 0) {
                        return g;
                    }
                    Timer gameTimer = new Timer();
                    gameTimer.schedule(new GameTimerTask(g.getId(), this.gameRepository, this), g.getRoundTime() * 1000L);
                    this.gameTimers.put(g.getId(), gameTimer);
                    return g;
                })
                .flatMap(this.gameRepository::saveGame)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(g -> new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        gameRepository.broadcastGame(g).subscribe();
                    }
                }, 500))
                .log();

    }

    public Mono<GameRound> submitWord(String word, Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(this.gameRepository::getGame)
                .publishOn(Schedulers.boundedElastic())
                .zipWith(player, (g, p) -> {
                    GameRound gr = g.guess(p, word);
                    if (g.getPlayers().stream().allMatch(players -> g.getGameStatus(players).equals(GameStatus.WAITING))) {
                        g.endRound();
                        if (g.getMaxTime() != 0 && this.gameTimers.get(g.getId()) != null) {
                            this.gameTimers.get(g.getId()).cancel();
                            this.gameTimers.get(g.getId()).purge();
                            restartTimer(g);
                        }
                    }
                    return this.gameRepository.saveGame(g).thenReturn(gr);
                })
                .flatMap(gameRound -> gameRound)
                .log();

    }

    public Mono<GameStats> getConclusion(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(this.gameRepository::getGame)
                .zipWith(player, Game::concludeGame)
                .publishOn(Schedulers.boundedElastic())
                .zipWith(player, (gC, p) -> {
                    this.gameRepository.getGame(p.getLobbyId())
                            .publishOn(Schedulers.boundedElastic())
                            .map(g -> {
                                if (g.getPlayers().stream().allMatch(players -> g.getGameStatus(players).equals(GameStatus.WAITING))) {
                                    g.getPlayers().forEach(players -> g.setGameStatus(players, GameStatus.GUESSING));
                                    this.gameRepository.saveGame(g).subscribe();
                                    this.gameRepository.broadcastGameStatus(g, GameStatus.GUESSING).subscribe();
                                }
                                return g;
                            })
                            .subscribe();
                    return gC;
                })
                .log();

    }

    public Flux<GameStatus> getGameStatus(String id, Mono<Player> player) {

        return player
                .flatMapMany(p -> this.gameRepository.getGameStatusStream(id, p))
                .log();

    }

    public Flux<GameRound[]> getOpponentGameRounds(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMapMany(this.gameRepository::getGameStream)
                .zipWith(player, Game::getCurrentOpponentGameRounds)
                .publishOn(Schedulers.boundedElastic())
                .repeat()
                .doFinally(ignored -> player
                        .flatMap(p -> this.gameRepository.getGame(p.getLobbyId()))
                        .publishOn(Schedulers.boundedElastic())
                        .zipWith(player, (g, p) -> {
                            if (g.getPlayers().stream().anyMatch(players -> g.getGameStatus(players).equals(GameStatus.FINISHED))) {
                                return g;
                            }
                            g.getPlayers().remove(p);

                            if (g.getPlayers().isEmpty()) {
                                if (this.gameTimers.get(g.getId()) != null) {
                                    this.gameTimers.remove(g.getId());
                                }
                            }
                            if (g.getPlayers().stream().allMatch(players -> g.getGameStatus(players).equals(GameStatus.WAITING))) {
                                g.endRound();
                                g.getPlayers().forEach(players -> g.setGameStatus(players, GameStatus.GUESSING));
                                this.gameRepository.broadcastGameStatus(g, GameStatus.GUESSING).subscribe();
                            }
                            this.gameRepository.saveGame(g).subscribe();
                            return g;
                        })
                        .subscribe())
                .log();

    }

    public Mono<Boolean> markStandBy(Mono<Player> player) {
        return player.mapNotNull(Player::getLobbyId)
                .flatMap(this.lobbyRepository::getLobby)
                .filter(l -> l.getGameCategory() == GameCategory.SOLO)
                .flatMap(l -> initializeGame(player))
                .then(player)
                .map(Player::getLobbyId)
                .flatMap(this.lobbyRepository::getLobby)
                .filter(l -> l.getGameCategory() != GameCategory.SOLO)
                .zipWith(player)
                .filter(t -> t.getT1().getGame().getGameStatus(t.getT2()) != GameStatus.GUESSING)
                .flatMap(t -> {
                    // OWNER INIT
                    if (t.getT1().getOwner().getId().equals(t.getT2().getId()) &&
                            t.getT1().getGame().getGameStatus(t.getT2()) == null) {
                        t.getT1().getPlayers().forEach(p -> t.getT1().getGame().setGameStatus(p, GameStatus.SYNCING));
                        return Mono.defer(() -> Mono.just(t));
                    }
                    if (t.getT1().getGame().getGameStatus(t.getT2()) == GameStatus.SYNCING) {
                        t.getT1().getGame().setGameStatus(t.getT2(), GameStatus.GUESSING);
                        return Mono.defer(() -> Mono.just(t));
                    }
                    return Mono.defer(Mono::empty);
                })
                .flatMap(lp -> this.lobbyRepository.saveLobby(lp.getT1()))
                .flatMap(l -> l.getPlayers().stream().anyMatch(p -> l.getGame().getGameStatus(p) == GameStatus.SYNCING) ?
                        this.gameRepository.saveGame(l.getGame())
                        :
                        initializeGame(player)
                )
                .switchIfEmpty(player.map(Player::getLobbyId).flatMap(this.lobbyRepository::getLobby).map(Lobby::getGame))
                .onErrorMap(e -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went terribly wrong."))
                .log()
                .then(Mono.just(true));
    }

    public void restartTimer(Game g) {
        if (g.getPlayers().stream().noneMatch(players -> g.getGameStatus(players).equals(GameStatus.FINISHED))) {
            Timer gameTimer = new Timer();
            gameTimer.schedule(new GameTimerTask(g.getId(), this.gameRepository, this), g.getRoundTime() * 1000L);
            this.gameTimers.put(g.getId(), gameTimer);
        }
    }
}