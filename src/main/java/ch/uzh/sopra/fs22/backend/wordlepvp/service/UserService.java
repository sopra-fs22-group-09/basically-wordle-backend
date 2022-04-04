package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.UserRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.validator.RegisterInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = User.builder()
                .passwordHash(input.getPassword())  // TODO: Hash!
                .username(input.getUsername())
                .email(input.getEmail())
                .build();

        return this.userRepository.saveAndFlush(user);
    }
}
