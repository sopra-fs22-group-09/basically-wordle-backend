package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameCategory;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.UserRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.LobbyService;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.PlayerService;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@GraphQlTest(LobbyController.class)
//@ContextConfiguration()
@ActiveProfiles("test")
@Import({LobbyRepository.class})
//@AutoConfigureTestEntityManager
public class LobbyControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

//    @MockBean
//    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;
//
//    @MockBean
//    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private LobbyService lobbyService;

    @MockBean
    private LobbyRepository lobbyRepository;

    @MockBean
    private PlayerService playerService;

//    @InjectMocks
    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @SpyBean
    private LobbyController injectedLobbyController;

    String authHeader() {
        return "Bearer Toast";
    }

//    @Test
//    void createLobby() {
//        this.graphQlTester.document("{greetingMono}")
//                .execute()
//                .path("greetingMono")
//                .entity(String.class)
//                .isEqualTo("Hello!");
//    }
//
//    @Test
//    void joinLobbyById() {
//        this.graphQlTester.document("{greetingsFlux}")
//                .execute()
//                .path("greetingsFlux")
//                .entityList(String.class)
//                .containsExactly("Hi!", "Bonjour!", "Hola!", "Ciao!", "Zdravo!");
//    }
//
    @Test
    void lobby() {
        Flux<Lobby> result = this.graphQlTester.documentName("lobby")
                .executeSubscription()
                .toFlux("lobby", Lobby.class)
                .contextWrite(Context.of("Authorization", "Bearer Toast"));
//                .contextWrite(context -> context.put("Authorization", "Bearer Toast"));

        Player expectedPlayer = Player.builder()
                .id("caffbeef-dead-beef-caff-deadbeefcaff")
                .name("Jeff")
                .build();

        Lobby expected = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .size(1)
                .name("TestLobby1")
                .gameCategory(GameCategory.PVP)
                .players(new HashSet<>(
                        Set.of(
                                expectedPlayer
                        )
                ))
                .build();

//        given(AuthorizationHelper.extractAuthToken(authHeader())).willReturn("deadbeee-dead-beef-caff-deadbeefcaff");
//        given(userService.getFromToken("deadbeee-dead-beef-caff-deadbeefcaff")).willReturn(expectedPlayer);
//        doReturn(authHeader()).when(injectedLobbyController).lobby(anyString());
        when(lobbyRepository.getLobbyStream(expected.getId())).thenReturn(Flux.just(expected));

        StepVerifier.create(result)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getLobbies() {
        this.graphQlTester.document("query { getLobbies }")
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).hasSize(0));
    }

    @Test
    void anonymousThenUnauthorized() {
        this.graphQlTester.document("query { getLobbies }")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
                });
    }

//    @Test
//    void authHeaderPresentSuccess() {
//        this.graphQlTester.document("query { getLobbies }")
//                .execute()
//                .errors()
//                .satisfy(errors -> {
//                    assertThat(errors).hasSize(1);
//                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
//                });
//
//        StepVerifier.create(result)
//                .expectAccessibleContext()
//                .contains("Authorization", "Bearer Toast")
//                .then()
//                .verifyComplete();
//    }

}