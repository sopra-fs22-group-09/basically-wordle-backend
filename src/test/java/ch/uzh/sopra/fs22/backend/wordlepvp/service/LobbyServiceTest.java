package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.logic.Game;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.*;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.gameModes.SonicFast;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.GameRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.PlayerRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.GameSettingsInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LobbyInput;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DataRedisTest
@ActiveProfiles("test")
public class LobbyServiceTest {

    @Mock
    LobbyRepository lobbyRepository;

    @Mock
    GameRepository gameRepository;

    @Mock
    PlayerRepository playerRepository;

    @InjectMocks
    LobbyService lobbyService;

    @Test
    public void getLobbyByIdTest() {

        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.hasLobby(Mockito.anyString())).thenReturn(Mono.just(true));

        Mono<Lobby> lobby = this.lobbyService.getLobbyById("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void getLobbyByIdTestFail() {

        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.hasLobby(Mockito.anyString())).thenReturn(Mono.just(false));

        Mono<Lobby> lobby = this.lobbyService.getLobbyById("deadbeef-dead-beef-caff-deadbeefcaff");

        StepVerifier.create(lobby)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void initializeLobbyTest() {

        Player testPlayer = Player.builder().build();
        LobbyInput testLobbyInput = new LobbyInput();
        testLobbyInput.setName("deadbeef");
        testLobbyInput.setSize(2);
        testLobbyInput.setGameCategory(GameCategory.PVP);
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();

        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Mono<Lobby> lobby = this.lobbyService.initializeLobby(testLobbyInput, Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void initializeLobbyTestFail() {

        Player testPlayer = Player.builder().build();
        LobbyInput testLobbyInput = new LobbyInput();
        testLobbyInput.setName("deadbeef");
        testLobbyInput.setSize(10);
        testLobbyInput.setGameCategory(GameCategory.PVP);
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();

        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        assertThrows(ResponseStatusException.class, () -> {
            Mono<Lobby> lobby = this.lobbyService.initializeLobby(testLobbyInput, Mono.just(testPlayer));
            StepVerifier.create(lobby)
                    .verifyComplete();
        });
    }

    @Test
    public void reinitializeLobbyTest() {

        Player testPlayer = Player.builder()
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        LobbyInput testLobbyInput = new LobbyInput();
        testLobbyInput.setName("deadbeef");
        testLobbyInput.setSize(2);
        testLobbyInput.setGameCategory(GameCategory.PVP);
        Set<Player> testPlayers = new HashSet<>();
        testPlayers.add(testPlayer);
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(testPlayers)
                .build();

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));
        when(gameRepository.broadcastGameStatus(Mockito.any(), Mockito.any())).thenReturn(Mono.just(0L));

        Mono<Boolean> lobbyReinitialized = this.lobbyService.reinitializeLobby(Mono.just(testPlayer));

        StepVerifier.create(lobbyReinitialized)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void addPlayerToLobbyTest() {

        Player testPlayer = Player.builder().build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        testLobby.getPlayers().add(testPlayer);

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(playerRepository.savePlayer(Mockito.any())).thenReturn(Mono.just(testPlayer));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Mono<Lobby> lobby = this.lobbyService.addPlayerToLobby("deadbeef-dead-beef-caff-deadbeefcaff", Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void addPlayerToLobbyTestFailInGame() {

        Player testPlayer = Player.builder().build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.INGAME)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        testLobby.getPlayers().add(testPlayer);

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(playerRepository.savePlayer(Mockito.any())).thenReturn(Mono.just(testPlayer));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Mono<Lobby> lobby = this.lobbyService.addPlayerToLobby("deadbeef-dead-beef-caff-deadbeefcaff", Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void addPlayerToLobbyTestFailFull() {

        Player testPlayer = Player.builder().build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.FULL)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(playerRepository.savePlayer(Mockito.any())).thenReturn(Mono.just(testPlayer));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Mono<Lobby> lobby = this.lobbyService.addPlayerToLobby("deadbeef-dead-beef-caff-deadbeefcaff", Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void changeLobbyGameModeTest() {

        Player testPlayer = Player.builder()
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(testPlayer)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        GameSettingsInput testGameSettingsInput = new GameSettingsInput();
        testGameSettingsInput.setGameMode(GameMode.SONICFAST);

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Mono<Lobby> lobby = this.lobbyService.changeLobby(testGameSettingsInput, Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void changeLobbyTestFailOwner() {

        Player testPlayer = Player.builder()
                .id("testPlayer")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Player testOwner = Player.builder()
                .id("testOwner")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(testOwner)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        GameSettingsInput testGameSettingsInput = new GameSettingsInput();
        testGameSettingsInput.setGameMode(GameMode.SONICFAST);

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Mono<Lobby> lobby = this.lobbyService.changeLobby(testGameSettingsInput, Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void changeLobbyTestFailCategory() {

        Player testPlayer = Player.builder()
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(testPlayer)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();
        GameSettingsInput testGameSettingsInput = new GameSettingsInput();
        testGameSettingsInput.setGameMode(GameMode.CLASSIC);

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Mono<Lobby> lobby = this.lobbyService.changeLobby(testGameSettingsInput, Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void changeLobbyGameSettingsTest() {

        Player testPlayer = Player.builder()
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Game testGame = new SonicFast();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(testPlayer)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameMode.SONICFAST)
                .game(testGame)
                .players(new HashSet<>())
                .build();
        GameSettingsInput testGameSettingsInput = new GameSettingsInput();
        testGameSettingsInput.setGameMode(GameMode.SONICFAST);
        testGameSettingsInput.setAmountRounds(3);
        testGameSettingsInput.setRoundTime(180);

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Mono<Lobby> lobby = this.lobbyService.changeLobby(testGameSettingsInput, Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void changeLobbyGameSettingsTestOverdrive() {

        Player testPlayer = Player.builder()
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Game testGame = new SonicFast();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(testPlayer)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameMode.SONICFAST)
                .game(testGame)
                .players(new HashSet<>())
                .build();
        GameSettingsInput testGameSettingsInput = new GameSettingsInput();
        testGameSettingsInput.setGameMode(GameMode.SONICFAST);
        testGameSettingsInput.setAmountRounds(15);
        testGameSettingsInput.setRoundTime(600);

        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Mono<Lobby> lobby = this.lobbyService.changeLobby(testGameSettingsInput, Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void subscribeLobbyTest() {

        Player testPlayer = Player.builder()
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(testPlayer)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameMode.SONICFAST)
                .players(new HashSet<>())
                .build();

        when(lobbyRepository.getLobbyStream(Mockito.anyString())).thenReturn(Flux.just(testLobby));
        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Flux<Lobby> lobby = this.lobbyService.subscribeLobby(testLobby.getId(), Mono.just(testPlayer));

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void subscribeLobbyTestNewOwner() {

        Player testPlayer = Player.builder()
                .id("testPlayer")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Player testOwner = Player.builder()
                .id("testOwner")
                .lobbyId("deadbeef-dead-beef-caff-deadbeefcaff")
                .build();
        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(testOwner)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameMode.SONICFAST)
                .players(new HashSet<>())
                .build();
        testLobby.getPlayers().add(testOwner);
        testLobby.getPlayers().add(testPlayer);

        when(lobbyRepository.getLobbyStream(Mockito.anyString())).thenReturn(Flux.just(testLobby));
        when(lobbyRepository.getLobby(Mockito.anyString())).thenReturn(Mono.just(testLobby));
        when(lobbyRepository.saveLobby(Mockito.any())).thenReturn(Mono.just(testLobby));

        Flux<Lobby> lobby = this.lobbyService.subscribeLobby(testLobby.getId(), Mono.just(testOwner));

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void getLobbiesTest() {

        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameMode.SONICFAST)
                .players(new HashSet<>())
                .build();

        when(lobbyRepository.getAllLobbies()).thenReturn(Flux.just(testLobby));

        Flux<Lobby> lobby = this.lobbyService.getLobbies();

        StepVerifier.create(lobby)
                .expectNext(testLobby)
                .verifyComplete();
    }

    @Test
    public void subscribeLobbiesTest() {

        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .status(LobbyStatus.OPEN)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameMode.SONICFAST)
                .players(new HashSet<>())
                .build();
        List<Lobby> testLobbies = new ArrayList<>();
        testLobbies.add(testLobby);

        when(lobbyRepository.getAllLobbiesStream()).thenReturn(Flux.just(testLobbies));

        Flux<List<Lobby>> lobbies = this.lobbyService.subscribeLobbies();

        StepVerifier.create(lobbies)
                .expectNext(testLobbies)
                .verifyComplete();
    }

    @Test
    public void receiveLobbyInvitesTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .build();
        LobbyInvite testLobbyInvite = LobbyInvite.builder().build();

        when(lobbyRepository.getInvitesStream(Mockito.any())).thenReturn(Flux.just(testLobbyInvite));

        Flux<LobbyInvite> lobbyInvite = this.lobbyService.receiveLobbyInvites(testUser);

        StepVerifier.create(lobbyInvite)
                .expectNext(testLobbyInvite)
                .verifyComplete();
    }

    @Test
    public void sendLobbyInviteTest() {

        User testSender = User.builder()
                .id(UUID.randomUUID())
                .build();
        User testReceiver = User.builder()
                .id(UUID.randomUUID())
                .build();

        when(lobbyRepository.inviteToLobby(Mockito.any())).thenReturn(Mono.just(true));

        Mono<Boolean> sentLobbyInvite = this.lobbyService.sendLobbyInvite("deadbeef-dead-beef-caff-deadbeefcaff", testReceiver, testSender);

        StepVerifier.create(sentLobbyInvite)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void createGameTestFail() {

        Lobby testLobby = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .name("deadbeef")
                .size(2)
                .owner(null)
                .status(LobbyStatus.FULL)
                .gameCategory(GameCategory.PVP)
                .gameMode(GameCategory.PVP.getDefaultGameMode())
                .players(new HashSet<>())
                .build();

        assertThrows(ResponseStatusException.class, () -> lobbyService.createGame(testLobby.getId(), GameMode.TEST));
    }
}
