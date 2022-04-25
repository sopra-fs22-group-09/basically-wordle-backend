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

    public Mono<Player> createPlayer(User user, String id) {

        Player player = Player.builder()
                .id(user.getId().toString())
                .name(user.getUsername())
                .avatarID(user.getAvatarID())
                .lobbyId(id)
                .build();

        return playerRepository.savePlayer(player);
    }

    public Mono<Player> getFromToken(String token) {

        return playerRepository.findById(authRepository.getUserID(token).toString())
                .doOnNext(player -> {
                    if (player == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player does not exist!");
                    }
                });
    }
}