package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByResetToken(String resetToken);
    @Query("select u.friends from User u where u.id = ?1")
    List<User> findAllFriendsById(UUID id);
    @Query("select u.friends, f from User u, User f where f member u.friends and u.id = ?1 and f.status = ?2")
    List<User> findFriendsByIdAndStatus(UUID id, UserStatus status);
}
