package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("userRepository")
@EnableJpaRepositories
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByResetToken(String resetToken);
}
