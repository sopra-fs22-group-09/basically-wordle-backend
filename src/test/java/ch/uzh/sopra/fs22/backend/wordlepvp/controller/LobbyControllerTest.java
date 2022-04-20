package ch.uzh.sopra.fs22.backend.wordlepvp.controller;

import ch.uzh.sopra.fs22.backend.wordlepvp.RedisConfig;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.GameCategory;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.Lobby;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.LobbyRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.UserRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@GraphQlTest(LobbyController.class)
@ActiveProfiles("test")
@Import({LobbyRepository.class, RedisConfig.class})
//@AutoConfigureTestEntityManager
public class LobbyControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private LobbyRepository lobbyRepository;

//    @InjectMocks
    @MockBean
    private UserService userService;

    @Mock
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
        Flux<Lobby> result = this.graphQlTester.document("""
                        subscription {
                          lobby {
                            id
                            size
                            name
                            gameCategory
                            players {
                              id
                              username
                            }
                          }
                        }""")
                .executeSubscription()
                .toFlux("lobby", Lobby.class);

        given(lobbyRepository.getLobbyStream("deadbeef-dead-beef-caff-deadbeefcaff", User.builder()
                .id(UUID.fromString("caffbeef-dead-beef-caff-deadbeefcaff"))
                .username("Jeff")
                .build())).willReturn(result);

        Lobby expected = Lobby.builder()
                .id("deadbeef-dead-beef-caff-deadbeefcaff")
                .size(1)
                .name("TestLobby1")
                .gameCategory(GameCategory.PVP)
                .players(new HashSet<>(
                        Set.of(
                                User.builder()
                                        .id(UUID.fromString("caffbeef-dead-beef-caff-deadbeefcaff"))
                                        .username("Jeff")
                                        .build()
                        )
                ))
                .build();

        StepVerifier.create(result)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getLobbies() {
        Flux<GraphQlTester.Response> result = this.graphQlTester.document("subscription { greetings }")
                .executeSubscription()
                .toFlux();

        StepVerifier.create(result)
                .consumeNextWith(response -> response.path("greetings").hasValue())
                .consumeNextWith(response -> response.path("greetings").matchesJson("\"Bonjour!\""))
                .consumeNextWith(response -> response.path("greetings").matchesJson("\"Hola!\""))
                .expectNextCount(2)
                .verifyComplete();
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

}