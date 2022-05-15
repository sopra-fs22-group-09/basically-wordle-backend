package ch.uzh.sopra.fs22.backend.wordlepvp.service;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.Player;
import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.AuthRepository;
import ch.uzh.sopra.fs22.backend.wordlepvp.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final AuthRepository authRepository;

    @Autowired
    public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository, AuthRepository authRepository) {
        this.playerRepository = playerRepository;
        this.authRepository = authRepository;
    }

    public Mono<Player> createPlayer(User user, String lobbyId) {

        Player player = Player.builder()
                .id(user.getId().toString())
                .name(user.getUsername())
                .avatarID(user.getAvatarID())
                .lobbyId(lobbyId)
                .build();

        return this.playerRepository.savePlayer(player);
    }

    public Mono<Player> createPlayer(String lobbyId) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);
        String username = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + " (Guest)";

        Player player = Player.builder()
                .id(UUID.randomUUID().toString())
                .name(username)
                .lobbyId(lobbyId)
                .build();

        return this.playerRepository.savePlayer(player);
    }

    public String authorize(Player player) {
        return authRepository.setAuthToken(player);
    }

    public Mono<Player> getFromToken(String token) {

        return this.playerRepository.findById(this.authRepository.getUserID(token).toString())
                .doOnNext(player -> {
                    if (player == null) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are unauthorized!");
                    }
                });
    }
}