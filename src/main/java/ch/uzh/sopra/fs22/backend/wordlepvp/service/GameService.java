package ch.uzh.sopra.fs22.backend.wordlepvp.service;

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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final LobbyRepository lobbyRepository;
    private final WordsRepository wordsRepository;
    private final Map<Game, Timer> gameTimers;

    @Autowired
    public GameService(GameRepository gameRepository, LobbyRepository lobbyRepository, WordsRepository wordsRepository) {
        this.gameRepository = gameRepository;
        this.lobbyRepository = lobbyRepository;
        this.wordsRepository = wordsRepository;
        this.gameTimers = new HashMap<>();
    }

    public Mono<Game> initializeGame(Mono<Player> player) {

        return player.zipWhen(p -> this.lobbyRepository.getLobby(p.getLobbyId()))
                .filter(t -> t.getT2().getGameCategory() == GameCategory.SOLO
                        && t.getT2().getGame().getGameStatus(t.getT1()) != GameStatus.GUESSING)
                .doOnNext(t -> t.getT2().getGame().setGameStatus(t.getT1(), GameStatus.GUESSING))
                .zipWhen(t -> this.gameRepository.saveGame(t.getT2().getGame()), (lp, p) -> lp)
                .zipWhen(t -> this.lobbyRepository.saveLobby(t.getT2()), (lp, p) -> lp)
                .switchIfEmpty(player.zipWhen(p -> this.lobbyRepository.getLobby(p.getLobbyId())))
                .filter(t -> t.getT2().getGame().getGameStatus(t.getT1()) == GameStatus.GUESSING)
                .map(l -> l.getT2().getGame().start(l.getT2().getPlayers(), this.wordsRepository.getRandomWords(250)))
                .map(g -> {
                    if (g.getMaxTime() == 0) {
                        return g;
                    }
                    Timer gameTimer = new Timer();
                    gameTimer.schedule(new GameTimerTask(g, this.gameRepository), g.getRoundTime() * 1000L);
                    this.gameTimers.put(g, gameTimer);
                    return g;
                })
                .flatMap(this.gameRepository::saveGame)
                .log();

    }

    public Mono<GameRound> submitWord(String word, Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(this.gameRepository::getGame)
                .zipWith(player, (g, p) -> {
                    GameRound gr = g.guess(p, word);
                    if (gr.getFinish() != 0L) {
                        if (g.getPlayers().stream().allMatch(players -> g.getGameStatus(players).equals(GameStatus.WAITING))) {
                            gr = g.endRound();
                            this.gameTimers.get(g).cancel();
                            this.gameTimers.get(g).purge();
                            if (g.getPlayers().stream().noneMatch(players -> g.getGameStatus(players).equals(GameStatus.FINISHED))) {
                                this.gameTimers.get(g).schedule(new GameTimerTask(g, this.gameRepository), g.getRoundTime() * 1000L);
                            }
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
                .log();

    }

    public Flux<GameStatus> getGameStatus(String id, Mono<Player> player) {

        return this.gameRepository.getGameStatusStream(id);

/*        return player
                .flatMapMany(this.gameRepository::getGameStatusStream)
                .log();*/

    }

    public Flux<GameRound[]> getOpponentGameRounds(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMapMany(this.gameRepository::getGameStream)
                .zipWith(player, Game::getCurrentOpponentGameRounds)
                .repeat(() -> true)
                .log();

    }

    public Mono<Game> markStandBy(Mono<Player> player) {
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
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                        "There is currently no sync in progress for this lobby.")))
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "You are not currently in a lobby.")))
                .flatMap(lp -> this.lobbyRepository.saveLobby(lp.getT1()))
                .flatMap(l -> l.getPlayers().stream().anyMatch(p -> l.getGame().getGameStatus(p) == GameStatus.SYNCING) ?
                        // TODO: Selectively notify players?
                        this.gameRepository.saveGame(l.getGame())
                        :
                        initializeGame(player)
                )
                .switchIfEmpty(player.map(Player::getLobbyId).flatMap(this.lobbyRepository::getLobby).map(Lobby::getGame))
                .onErrorMap(e -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went terribly wrong."))
                .log();
                //.then(Mono.just(true));
    }
}