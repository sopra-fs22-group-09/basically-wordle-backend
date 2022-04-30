package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.WordsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final LobbyRepository lobbyRepository;
    private final WordsRepository wordsRepository;

    @Autowired
    public GameService(GameRepository gameRepository, LobbyRepository lobbyRepository, WordsRepository wordsRepository) {
        this.gameRepository = gameRepository;
        this.lobbyRepository = lobbyRepository;
        this.wordsRepository = wordsRepository;
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
                .flatMapMany(this.gameRepository::getGameStream)
                .zipWith(player, Game::getCurrentGameStatus)
                .log();

    }

    public Flux<GameRound[]> getOpponentGameRounds(Mono<Player> player) {

        return player.map(Player::getLobbyId)
                .flatMapMany(this.gameRepository::getGameStream)
                .zipWith(player, Game::getCurrentOpponentGameRounds)
                .log();

    }
}