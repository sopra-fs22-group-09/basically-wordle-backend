package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameCategory;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.UserRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import ch.uzh.sopra.fs22.backend.wordlepvp.util.AuthorizationHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@GraphQlTest(LobbyController.class)
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
    private LobbyRepository lobbyRepository;

//    @InjectMocks
    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

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
                // FIXME: Jesus Christ! Why does Authorization appear as a new KV pair inside the top-level map? D:
//                .contextWrite(Context.of("Authorization", "Bearer Toast"))
                .contextWrite(context -> context.put("Authorization", "Bearer Toast"));

        User expectedPlayer = User.builder()
                .id(UUID.fromString("caffbeef-dead-beef-caff-deadbeefcaff"))
                .username("Jeff")
                .build();

        given(AuthorizationHelper.extractAuthToken(Mockito.anyString())).willReturn("deadbeee-dead-beef-caff-deadbeefcaff");
//        given(userService.getFromToken("deadbeee-dead-beef-caff-deadbeefcaff")).willReturn(expectedPlayer);
        when(lobbyRepository.getLobbyStream(Mockito.anyString(), Mockito.any())).thenReturn(Flux.empty());

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