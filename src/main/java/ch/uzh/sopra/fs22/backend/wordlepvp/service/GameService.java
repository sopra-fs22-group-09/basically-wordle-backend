package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.WordsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final LobbyRepository lobbyRepository;
    private final WordsRepository wordsRepository;

    private final ReactiveRedisTemplate<String, GameStatus> reactiveRedisTemplate;

    @Autowired
    public GameService(GameRepository gameRepository, LobbyRepository lobbyRepository, WordsRepository wordsRepository, ReactiveRedisTemplate<String, GameStatus> reactiveRedisTemplate) {
        this.gameRepository = gameRepository;
        this.lobbyRepository = lobbyRepository;
        this.wordsRepository = wordsRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Game> initializeGame(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(this.lobbyRepository::getLobby)
                .map(l -> l.getGame().start(l.getPlayers(), this.wordsRepository.getRandomWords(250)))
                .flatMap(this.gameRepository::saveGame)
//                .flatMap(g -> this.reactiveRedisTemplate.convertAndSend("gameSync/" + g.getId(), GameStatus.GUESSING).thenReturn(g))
                .log();

    }

    public Mono<GameRound> submitWord(String word, Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(this.gameRepository::getGame)
                .zipWith(player, (g, p) -> {
                    GameRound gr = g.guess(p, word);
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

    public Flux<GameStatus> getGameStatus(Mono<Player> player) {

        return player.map(Player::getId)
                .flatMapMany(pid -> this.reactiveRedisTemplate.listenToChannel("gameSync/" + pid))
                .map(ReactiveSubscription.Message::getMessage)
                .log();

    }

    public Flux<GameRound[]> getOpponentGameRounds(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMapMany(this.gameRepository::getGameStream)
                .zipWith(player, Game::getCurrentOpponentGameRounds)
                .repeat(() -> true)
                .log();

    }

    public Mono<Game> markStandBy(Mono<Player> player) {
        //return this.initializeGame(player);
        return player.mapNotNull(Player::getLobbyId)
                .flatMap(this.lobbyRepository::getLobby)
                .zipWith(player)
                .filter(t -> t.getT1().getGame().getGameStatus(t.getT2()) != GameStatus.GUESSING)
                .switchIfEmpty(Mono.empty())
                .flatMap(t -> {
                    // OWNER INIT
                    if (t.getT1().getOwner().getId().equals(t.getT2().getId()) &&
                            t.getT1().getGame().getGameStatus(t.getT2()) == null) {
                        t.getT1().getPlayers().forEach(p -> t.getT1().getGame().setGameStatus(p, GameStatus.SYNCING));
                        return Mono.defer(() -> Mono.just(t));
                    }
                    if (t.getT1().getGame().getGameStatus(t.getT2()) == GameStatus.SYNCING) {
                        // TODO: Should this be waiting?
                        t.getT1().getGame().setGameStatus(t.getT2(), GameStatus.GUESSING);
                        return Mono.defer(() -> Mono.just(t));
                    }
                    return Mono.defer(Mono::empty);
                })
                //.doOnNext(t -> t.getT1().getPlayers().forEach(p -> System.out.println("PLAYER: " + t.getT2().getName() + ", Status: " + t.getT1().getGame().getGameStatus(p))))
                .log()
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                        "There is currently no sync in progress for this lobby.")))
//                .doOnNext(t -> t.getT1().getGame().setGameStatus(t.getT2(), GameStatus.GUESSING))
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "You are not currently in a lobby.")))
                .flatMap(lp -> Mono.defer(() -> this.lobbyRepository.saveLobby(lp.getT1())))
                .flatMap(l -> l.getPlayers().stream().anyMatch(p -> l.getGame().getGameStatus(p).equals(GameStatus.SYNCING)) ?
                        // TODO: Selectively notify players!
//                        this.reactiveRedisTemplate.convertAndSend("gameSync/" + l.getGame().getId(),
//                                GameStatus.SYNCING).then(Mono.empty())
                        this.gameRepository.saveGame(l.getGame())
                        :
                        Mono.just(l))
//                .doOnNext(l -> l.getPlayers().forEach(p -> l.getGame().setGameStatus(p, GameStatus.GUESSING)))
                .flatMap(l -> initializeGame(player))
                //.doOnNext(t -> System.out.println("Game: " + t.playersSynced()))
                .onErrorMap(e -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went terribly wrong."))
                .log();
                //.then(Mono.just(true));
    }
}