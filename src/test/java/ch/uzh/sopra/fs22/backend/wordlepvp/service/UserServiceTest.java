package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.UserStatus;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.AuthRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.FriendsRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.UserRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LoginInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.RegisterInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetTokenInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataRedisTest
@ActiveProfiles("test")
public class UserServiceTest {

    private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder();

    @Mock
    UserRepository userRepository;

    @Mock
    AuthRepository authRepository;

    @Mock
    FriendsRepository friendsRepository;

    @InjectMocks
    UserService userService;

    @Test
    public void findByIdTest() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();

        when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testUser));
        Optional<User> user = this.userService.findById(testUserId);

        if (user.isEmpty()) {
            fail("User does not exist, even though he should.");
        }
        assertEquals(testUser, user.get());
    }

    @Test
    public void createUserTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .build();
        RegisterInput testRegisterInput = new RegisterInput();
        testRegisterInput.setUsername("testUser");
        testRegisterInput.setPassword("testPassword1?");
        testRegisterInput.setEmail("test@test.io");

        when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
        when(userRepository.saveAndFlush(Mockito.any())).thenReturn(testUser);

        User user = this.userService.createUser(testRegisterInput);

        assertEquals(testUser, user);
    }

    @Test
    public void validateUserTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .passwordHash(encoder.encode("testPassword1?"))
                .build();
        LoginInput testLoginInput = new LoginInput();
        testLoginInput.setUsername("testUser");
        testLoginInput.setPassword("testPassword1?");

        when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
        when(friendsRepository.broadcastFriendsEvent(Mockito.any())).thenReturn(Mono.empty());

        User user = this.userService.validateUser(testLoginInput);

        assertEquals(testUser, user);
    }

    @Test
    public void logoutTest() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();

        when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testUser));
        when(authRepository.getUserID(Mockito.any())).thenReturn(testUserId);
        when(friendsRepository.broadcastFriendsEvent(Mockito.any())).thenReturn(Mono.empty());

        boolean loggedOut = userService.logout("testToken");

        assertTrue(loggedOut);
    }

    @Test
    public void resetPasswordTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .build();
        ResetInput testResetInput = new ResetInput();
        testResetInput.setEmail("test@test.io");

        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(testUser);

        assertThrows(NullPointerException.class, () -> userService.resetPassword(testResetInput));
    }

    @Test
    public void resetWithTokenTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .build();
        ResetTokenInput testResetTokenInput = new ResetTokenInput();
        testResetTokenInput.setResetToken("testResetToken");
        testResetTokenInput.setPassword("testPassword1?");

        when(userRepository.findByResetToken(Mockito.anyString())).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.resetWithToken(testResetTokenInput));
    }

    @Test
    public void authorizeTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .build();

        when(authRepository.setAuthToken(Mockito.any())).thenReturn("testToken");

        assertEquals("testToken", userService.authorize(testUser));

    }

    @Test
    public void getFromToken() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();

        when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testUser));
        when(authRepository.getUserID(Mockito.any())).thenReturn(testUserId);
        User user = userService.getFromToken("testToken");
        assertEquals(testUser, user);

        when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> userService.getFromToken("testToken"));
    }

    @Test
    public void completeTutorialTest() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();

        when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testUser));
        when(authRepository.getUserID(Mockito.any())).thenReturn(testUserId);

        assertTrue(userService.completeTutorial("testToken"));
    }

    @Test
    public void addFriendTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .build();
        UUID testFriendId = UUID.randomUUID();
        User testFriend = User.builder()
                .id(testFriendId)
                .build();

        when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testFriend));
        when(userRepository.saveAndFlush(Mockito.any())).thenReturn(testUser);
        when(friendsRepository.broadcastFriendsEvent(Mockito.any())).thenReturn(Mono.empty());

        assertThrows(NullPointerException.class, () -> userService.addFriend(testFriendId.toString(), testUser));
    }

    @Test
    public void friendsByIdAndStatusTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .build();
        User testFriend = User.builder()
                .id(UUID.randomUUID())
                .build();
        UserStatus testStatus = UserStatus.ONLINE;
        List<User> testFriends = new ArrayList<>();
        testFriends.add(testFriend);

        when(userRepository.findFriendsByIdAndStatus(testUser.getId(), testStatus)).thenReturn(testFriends);

        assertEquals(testFriends, userService.friends(testStatus, testUser));
    }

    @Test
    public void friendsByIdTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .build();
        User testFriend = User.builder()
                .id(UUID.randomUUID())
                .build();
        List<User> testFriends = new ArrayList<>();
        testFriends.add(testFriend);

        when(userRepository.findAllFriendsById(testUser.getId())).thenReturn(testFriends);

        assertEquals(testFriends, userService.friends(testUser));
    }

    @Test
    public void setUserStatusTest() {

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .build();

        when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testUser));
        when(userRepository.saveAndFlush(Mockito.any())).thenReturn(testUser);
        when(friendsRepository.broadcastFriendsEvent(Mockito.any())).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> userService.setUserStatus(testUserId, UserStatus.ONLINE));

    }

    @Test
    public void getFriendsUpdatesTest() {

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .build();

        when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));
        when(friendsRepository.getFriendsStream(Mockito.any())).thenReturn(Flux.just(testUser));

        Flux<User> user = userService.getFriendsUpdates("testToken");
        StepVerifier.create(user)
                .expectNext(testUser)
                .verifyComplete();
    }
}
