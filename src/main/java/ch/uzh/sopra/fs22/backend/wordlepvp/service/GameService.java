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
                .log();

    }

    public Mono<GameRound> submitWord(String word, Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(this.gameRepository::getGame)
                .zipWith(player, (g, p) -> g.guess(p, word))
                .flatMap(this.gameRepository::saveGame)
                .zipWith(player, Game::getCurrentGameRound)
                .log();

    }

    public Mono<GameStats> getConclusion(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMap(this.gameRepository::getGame)
                .zipWith(player, Game::concludeGame)
                .log();

    }

    public Flux<GameStatus> getGameStatus(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMapMany(id -> this.reactiveRedisTemplate.listenToChannel("gamesync/" + id))
                .map(ReactiveSubscription.Message::getMessage)
                .log();

    }

    public Flux<GameRound[]> getOpponentGameRounds(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMapMany(this.gameRepository::getGameStream)
                .zipWith(player, Game::getCurrentOpponentGameRounds)
                .log();

    }

    // TODO: Race conditions?
    public Mono<Boolean> markStandBy(Mono<Player> player) {
        return player.mapNotNull(Player::getLobbyId)
                .flatMap(this.lobbyRepository::getLobby)
                .zipWith(player)
                .mapNotNull(t -> {
                    if (t.getT1().getOwner().getId().equals(t.getT2().getId())
                            && t.getT1().getGame().getStatus() == GameStatus.NEW) {
                        t.getT1().getGame().setStatus(GameStatus.PREPARING);
                        return t;
                    }
                    return t.getT1().getGame().getStatus() == GameStatus.PREPARING ? t : null;
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "There is currently no sync in progress for this lobby.")))
                .doOnNext(t -> t.getT2().setSynced(true))
                .log()
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "You are not currently in a lobby.")))
                .map(lp -> {
                    lp.getT1().getPlayers().add(lp.getT2());
                    return lp.getT1();
                })
                .flatMap(this.lobbyRepository::saveLobby)
                .log()
                .mapNotNull(t -> t.getPlayers().stream().allMatch(Player::isSynced) ? t : null)
                .doOnNext(t -> t.getGame().setStatus(GameStatus.GUESSING))
                .flatMap(t -> initializeGame(player))
                .map(g -> this.reactiveRedisTemplate
                        .convertAndSend("gamesync/" + g.getId(), g.getStatus()))
                .onErrorMap(e -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went terribly wrong."))
                .log()
                .then(Mono.just(true));
    }
}