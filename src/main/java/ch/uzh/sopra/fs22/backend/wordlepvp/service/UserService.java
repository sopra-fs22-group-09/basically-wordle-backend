package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.EmailService;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.UserStatus;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.UserRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LoginInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.RegisterInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetTokenInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder();

    private final EmailService emailService;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }



    public User createUser(RegisterInput input) {
        if (this.userRepository.findByUsername(input.getUsername()) != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This username is already taken.");

        var passwordCandidate = input.getPassword();

        if (passwordCandidate.length() < 5) {
            // TODO: Require alphanumeric, upper-, lowercase and special character(s)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password does not meet minimum password criteria.");
        }

        var encodedPassword = encoder.encode(passwordCandidate);

        User user = User.builder()
                .passwordHash(encodedPassword)
                .username(input.getUsername())
                .email(input.getEmail())
                .activated(true)
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
//        User.builder()
//                .id(UUID.randomUUID())
//                .activated(true)
//                .username("Tester")
//                .passwordHash("<SET_ME>")
//                .email("tester@oxv.io")
//                .avatarID(null)
//                .status(UserStatus.ONLINE)
//                .build();

        if (!passwordValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The username and password combination does not exist.");
        }

        userByUsername.setStatus(UserStatus.ONLINE);
        return userByUsername;
    }

    public void logout() {
        User user = userRepository.findByToken("");
    }

    public void resetPassword(ResetInput input) {
        User userByEmail = userRepository.findByEmail(input.getEmail());

        if (userByEmail == null) {
            return;
        }
        String resetToken = RandomStringUtils.random(48,true,true);
        userByEmail.setResetToken(resetToken);
        emailService.sendSimpleMessage(userByEmail.getEmail(), "Password Reset", "Hi " + userByEmail.getUsername() + ",\n\nplease go to http://localhost:3000/reset/tokenEntry and enter the following code to reset your password:\n\n" + resetToken);
    }

    public void resetWithToken(ResetTokenInput input) {
        User userByResetToken = userRepository.findByResetToken(input.getResetToken());

        var passwordCandidate = input.getPassword();

        if (userByResetToken == null || input.getResetToken() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The entered reset token is invalid.");
        }
        if (passwordCandidate.length() < 5) {
            // TODO: Require alphanumeric, upper-, lowercase and special character(s)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password does not meet minimum password criteria.");
        }

        var encodedPassword = encoder.encode(passwordCandidate);

        userByResetToken.setPasswordHash(encodedPassword);
        userByResetToken.setResetToken(null);
    }

    public UUID giveMeDaAuthToken(UUID userId) {
        // TODO: Do it properly, maybe save session in redis?
        return UUID.randomUUID();
    }
}