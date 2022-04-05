package ch.uzh.sopra.fs22.backend.wordlepvp.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(RegisterInput input) {
        if (this.userRepository.findByUsername(input.getUsername()) != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This username is already taken.");

        User user = User.builder()
            .passwordHash(input.getPassword())  // TODO: Hash!
            .username(input.getUsername())
            .email(input.getEmail())
            .build();

        return this.userRepository.saveAndFlush(user);
    }

    public User validateUser(LoginInput input) {
        User userByUsername = userRepository.findByUsername(input.getUsername());

        //TODO: Cannot compare passwordhash to password !!!
        if (userByUsername == null || Objects.equals(userByUsername.getPasswordHash(), input.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The username and password combination does not exist.");
        }

        //TODO: setUserStatus
        return userByUsername;
    }

    public void resetPassword(ResetInput input) {
        User userByEmail = userRepository.findByEmail(input.getEmail());

        if (userByEmail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no user registered with that Email.");
        }

        //TODO: generate and send password reset Email
    }
}