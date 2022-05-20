package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes.Classic;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes.WordsPP;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.WordsRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.Mockito.when;

@DataRedisTest
@ActiveProfiles("test")
public class GameServiceTest {

    @Mock
    GameRepository gameRepository;

    @Mock
    LobbyRepository lobbyRepository;

    @Mock
    WordsRepository wordsRepository;

    @InjectMocks
    GameService gameService;

    @Test
    public void initializeGameTest() {

        Player testPlayer = Player.builder()
                .id("testPlayer")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.SOLO)
                .gameMode(GameCategory.SOLO.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        Game testGame = new Classic();
        testGame.setId(testLobby.getId());
        testLobby.setGame(testGame);
        String[] testWords = {"Mules", "Monks", "Apple"};

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(gameRepository.saveGame(Mockito.any())).thenReturn(Mono.just(testGame));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));
        when(wordsRepository.getRandomWords(Mockito.anyInt())).thenReturn(testWords);

        Mono<Game> game = gameService.initializeGame(Mono.just(testPlayer));

        StepVerifier.create(game)
                .expectNext(testGame)
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void submitWordTest() {

        Player testPlayer = Player.builder()
                .id("testPlayer")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Set<Player> testPlayers = new HashSet<>();
        testPlayers.add(testPlayer);
        Game testGame = new WordsPP();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");
        testGame.setGameStatus(testPlayer, GameStatus.GUESSING);
        String[] testWords = {"Mules", "Monks", "Apple"};

        when(gameRepository.getGame(Mockito.anyString())).thenReturn(Mono.just(testGame));
        when(gameRepository.saveGame(Mockito.any())).thenReturn(Mono.just(testGame));

        testGame.start(testPlayers, testWords);
        GameRound testGameRound = null;
        try {
            Field privateField = testGame.getClass().getSuperclass().getDeclaredField("game");
            privateField.setAccessible(true);
            Map<Player, GameRound> game = (HashMap<Player, GameRound>) privateField.get(testGame);
            testGameRound = game.get(testPlayer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        assert testGameRound != null;

        Mono<GameRound> gameRound = gameService.submitWord("Mules", Mono.just(testPlayer));

        StepVerifier.create(gameRound)
                .expectNext(testGameRound)
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void submitWordSuccessTest() {

        Player testPlayer = Player.builder()
                .id("testPlayer")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Set<Player> testPlayers = new HashSet<>();
        testPlayers.add(testPlayer);
        Game testGame = new WordsPP();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");
        testGame.setGameStatus(testPlayer, GameStatus.GUESSING);
        String[] testWords = {"Mules"};

        when(gameRepository.getGame(Mockito.anyString())).thenReturn(Mono.just(testGame));
        when(gameRepository.saveGame(Mockito.any())).thenReturn(Mono.just(testGame));

        testGame.start(testPlayers, testWords);
        GameRound testGameRound = null;
        try {
            Field privateField = testGame.getClass().getSuperclass().getDeclaredField("game");
            privateField.setAccessible(true);
            Map<Player, GameRound> game = (HashMap<Player, GameRound>) privateField.get(testGame);
            testGameRound = game.get(testPlayer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        assert testGameRound != null;

        Mono<GameRound> gameRound = gameService.submitWord("Mules", Mono.just(testPlayer));

        StepVerifier.create(gameRound)
                .expectNext(testGameRound)
                .verifyComplete();
    }

    @Test
    public void getConclusionTest() {

        Player testPlayer = Player.builder()
                .id("testPlayer")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Set<Player> testPlayers = new HashSet<>();
        testPlayers.add(testPlayer);
        Game testGame = new WordsPP();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");
        String[] testWords = {"Mules", "Monks", "Apple"};
        GameStats testStats = new GameStats();

        when(gameRepository.getGame(Mockito.anyString())).thenReturn(Mono.just(testGame));

        testGame.start(testPlayers, testWords);

        Mono<GameStats> gameStats = gameService.getConclusion(Mono.just(testPlayer));

        StepVerifier.create(gameStats)
                .expectNext(testStats)
                .verifyComplete();
    }

    @Test
    public void getGameStatusTest() {

        Player testPlayer = Player.builder()
                .id("testPlayer")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();

        when(gameRepository.getGameStatusStream(Mockito.anyString(), Mockito.any())).thenReturn(Flux.just(GameStatus.GUESSING));

        Flux<GameStatus> gameStatus = gameService.getGameStatus("deadbeef-dead-beef-caff-deadbeefcaff", Mono.just(testPlayer));

        StepVerifier.create(gameStatus)
                .expectNext(GameStatus.GUESSING)
                .verifyComplete();
    }

    @Test
    public void getOpponentGameRoundsTest() {

        Player testPlayer = Player.builder()
                .id("testPlayer")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Set<Player> testPlayers = new HashSet<>();
        testPlayers.add(testPlayer);
        Game testGame = new WordsPP();
        testGame.setId("deadbeef-dead-beef-caff-deadbeefcaff");
        String[] testWords = {"Mules", "Monks", "Apple"};

        when(gameRepository.getGameStream(Mockito.anyString())).thenReturn(Flux.just(testGame));

        testGame.start(testPlayers, testWords);

        Flux<GameRound[]> gameRounds = gameService.getOpponentGameRounds(Mono.just(testPlayer));

        StepVerifier.create(gameRounds)
                .thenCancel()
                .verify();
    }

    @Test
    public void markStandByTest() {

        Player testPlayer = Player.builder()
                .id("testPlayer")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(testPlayer)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.SOLO)
                .gameMode(GameCategory.SOLO.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        Game testGame = new Classic();
        testGame.setId(testLobby.getId());
        testLobby.setGame(testGame);
        String[] testWords = {"Mules", "Monks", "Apple"};

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));
        when(gameRepository.saveGame(Mockito.any())).thenReturn(Mono.just(testGame));
        when(wordsRepository.getRandomWords(Mockito.anyInt())).thenReturn(testWords);

        Mono<Boolean> game = gameService.markStandBy(Mono.just(testPlayer));

        StepVerifier.create(game)
                .expectNext(true)
                .verifyComplete();
    }
}
