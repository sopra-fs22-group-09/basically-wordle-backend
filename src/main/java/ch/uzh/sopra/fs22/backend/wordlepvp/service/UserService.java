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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.security.SecureRandom;
import java.util.*;

@Service
@Transactional
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final FriendsRepository friendsRepository;
    private final AuthRepository authRepository;
    private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder();
    private final EmailService emailService;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository, FriendsRepository friendsRepository, AuthRepository authRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.friendsRepository = friendsRepository;
        this.authRepository = authRepository;
        this.emailService = emailService;
    }

    public Optional<User> findById(UUID userId) {
        try {
            return this.userRepository.findById(userId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UUID.");
        }
    }

    public User createUser(RegisterInput input) {
        if (this.userRepository.findByUsername(input.getUsername()) != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This username is already taken.");

        String passwordCandidate = input.getPassword();
        validateNewPassword(passwordCandidate);
        String encodedPassword = encoder.encode(passwordCandidate);

        User user = User.builder()
                .username(input.getUsername())
                .passwordHash(encodedPassword)
                .email(input.getEmail())
                .activated(true) // TODO only for production, remove before release!
                .build();

        user.setStatus(UserStatus.ONLINE);
        return this.userRepository.saveAndFlush(user);
    }

    public User validateUser(LoginInput input) {
        boolean passwordValid = false;
        User userByUsername = userRepository.findByUsername(input.getUsername());

        if (userByUsername != null) {
            passwordValid = encoder.matches(input.getPassword(), userByUsername.getPasswordHash());
        } else {
            encoder.matches(input.getPassword(), "ThisIsJustARandomPassword");
        }

// TODO: For guest access or as a fallback if email verification doesn't work.
/*
        User user = User.builder()
                .id(UUID.randomUUID())
                .activated(true)
                .username("Tester")
                .passwordHash("<SET_ME>")
                .email("tester@oxv.io")
                .avatarID(null)
                .status(UserStatus.ONLINE)
                .build();
*/

        if (!passwordValid)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The username and password combination does not exist.");

        userByUsername.setStatus(UserStatus.ONLINE);
        this.friendsRepository.broadcastFriendsEvent(userByUsername).subscribe();
        return userByUsername;
    }

    public boolean logout(String token) {
        User user = getFromToken(token);
        user.setStatus(UserStatus.OFFLINE);
        authRepository.expire(token);
        this.friendsRepository.broadcastFriendsEvent(user).subscribe();
        return true;
    }

    public void resetPassword(ResetInput input) {
        User userByEmail = userRepository.findByEmail(input.getEmail());
        String url = Objects.equals(activeProfile, "prod") ? "https://oxv.io" : "http://localhost:3000";

        if (userByEmail != null) {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String resetToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); // TODO Maybe random UUID?
            userByEmail.setResetToken(resetToken);
            emailService.sendSimpleMessage(userByEmail.getEmail(), "Password Reset",
                    String.format("Hi %s,\r\nPlease go to %s/reset/tokenEntry and enter the following code to reset your password:\r\n%s",
                            userByEmail.getUsername(), url, resetToken));
        }
    }

    public void resetWithToken(ResetTokenInput input) {
        User userByResetToken = userRepository.findByResetToken(input.getResetToken());
        String passwordCandidate = input.getPassword();

        if (userByResetToken == null || input.getResetToken() == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The entered reset token is invalid.");

        validateNewPassword(passwordCandidate);
        String encodedPassword = encoder.encode(passwordCandidate);
        userByResetToken.setPasswordHash(encodedPassword);
        userByResetToken.setResetToken(null);
    }

    public String authorize(User user) {
        return authRepository.setAuthToken(user);
    }

    public User getFromToken(String token) {
        Optional<User> user;
        try {
            user = userRepository.findById(authRepository.getUserID(token));
            if (user.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are unauthorized!");
            }
        } catch (Exception ignored) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are unauthorized!");
        }
        return user.get();
    }

    public boolean completeTutorial(String token) {
        User user = getFromToken(token);
        user.setTutorialCompleted(true);
        return true;
    }

    private void validateNewPassword(String newPassword) {
        boolean digit = false;
        boolean uppercase = false;
        boolean lowercase = false;
        boolean specialCharacters = false;

        if (newPassword.length() >= 5 && newPassword.length() <= 50) {
            for (int i = 0; i < newPassword.length(); ++i) {
                // TODO exit condition?
                // TODO better with regex?
                lowercase = lowercase || ((int) newPassword.charAt(i) >= 97 && (int) newPassword.charAt(i) <= 122);
                uppercase = uppercase || ((int) newPassword.charAt(i) >= 65 && (int) newPassword.charAt(i) <= 90);
                digit = digit || ((int) newPassword.charAt(i) >= 48 && (int) newPassword.charAt(i) <= 57);
                specialCharacters = specialCharacters || ((int) newPassword.charAt(i) >= 32 && (int) newPassword.charAt(i) <= 47) || // "space" ! " # $ % & ' ( ) * + , - . /
                        ((int) newPassword.charAt(i) >= 58 && (int) newPassword.charAt(i) <= 64) || // : ; < = > ? @
                        ((int) newPassword.charAt(i) >= 91 && (int) newPassword.charAt(i) <= 96) || // [ \ ] ^ _ `
                        ((int) newPassword.charAt(i) >= 123 && (int) newPassword.charAt(i) <= 126); // { | } ~
            }
        }
        if (!(digit && uppercase && lowercase && specialCharacters))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password does not meet minimum password criteria.");
    }

    public List<User> addFriend(String friendId, User user) {
        if (friendId == null || friendId.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No friendId provided.");
        Optional<User> friendToAdd;
        try {
            friendToAdd = this.userRepository.findById(UUID.fromString(friendId));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given friendId is invalid.");
        }

        // FIXME: Handle more edge cases
        if (friendToAdd.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given friendId is invalid.");

        if (friendToAdd.get().equals(user)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We already established that you have no friends.");

        if (user.getFriends().contains(friendToAdd.get())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user is already a friend.");

        // TODO: Notify other user that we're now friends.
        friendToAdd.get().getFriends().add(user);
        user.getFriends().add(friendToAdd.get());
        this.userRepository.saveAllAndFlush(List.of(user, friendToAdd.get()));
        this.friendsRepository.broadcastFriendsEvent(user).subscribe();
        return friends(user);
    }

    public List<User> friends(UserStatus status, User user) {
        return this.userRepository.findFriendsByIdAndStatus(user.getId(), status);
    }

    public List<User> friends(User user) {
        return this.userRepository.findAllFriendsById(user.getId());
    }

    public void setUserStatus(UUID userId, UserStatus status) {
        Optional<User> foundUser = findById(userId);
        if (foundUser.isEmpty()) return;
        foundUser.get().setStatus(status);
        User newUser = this.userRepository.saveAndFlush(foundUser.get());
        this.friendsRepository.broadcastFriendsEvent(newUser).subscribe();
    }

    public Flux<User> getFriendsUpdates(String token) {
        User user = getFromToken(token);
        if (user != null)
            return this.friendsRepository.getFriendsStream(user);
//                    .repeat();
        else
            return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to subscribe here!"));
    }
}