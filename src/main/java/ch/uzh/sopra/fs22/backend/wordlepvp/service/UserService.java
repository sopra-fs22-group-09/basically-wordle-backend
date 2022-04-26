package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.UserStatus;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.AuthRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.UserRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LoginInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.RegisterInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetTokenInput;
import org.apache.commons.lang3.RandomStringUtils;
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

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder();
    private final EmailService emailService;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository, AuthRepository authRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.emailService = emailService;
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
        return userByUsername;
    }

    public boolean logout(String token) {
        User user = getFromToken(token);
        user.setStatus(UserStatus.OFFLINE);
        authRepository.expire(token);
        return true;
    }

    public void resetPassword(ResetInput input) {
        User userByEmail = userRepository.findByEmail(input.getEmail());
        String url = Objects.equals(activeProfile, "prod") ? "https://oxv.io" : "http://localhost:3000";

        if (userByEmail != null) {
            String resetToken = RandomStringUtils.random(48,true,true); // TODO Maybe random UUID?
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested action could not be completed.");
            }
        } catch (Exception ignored) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fatal error: The requested action could not be completed.");
        }
        return user.get();
    }

//    public Mono<User> getFromToken(String token) {
//        Mono<User> user;
//        try {
//            user = Mono.justOrEmpty(userRepository.findById(authRepository.getUserID(token)));
////            if (user.isEmpty()) {
////                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fatal error: User could not be logged out. Try to sign in and out again.");
////            }
//        } catch (IllegalArgumentException ignored) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fatal error: User could not be logged out. Try to sign in and out again.");
//        }
//        return user;
//    }

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
}