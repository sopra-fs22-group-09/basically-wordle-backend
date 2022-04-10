package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.UserStatus;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.UserRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.LoginInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.RegisterInput;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.ResetInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder();

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
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

        if (!passwordValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The username and password combination does not exist.");
        }

        userByUsername.setStatus(UserStatus.ONLINE);
        return userByUsername;
    }

    public void resetPassword(ResetInput input) {
        User userByEmail = userRepository.findByEmail(input.getEmail());

        if (userByEmail == null) {
            return;
        }

        //TODO: generate and send password reset Email
    }
}