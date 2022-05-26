package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import ch.uzh.sopra.fs22.backend.wordlepvp.logic.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
@Builder
@RedisHash("lobbies")
@NoArgsConstructor
@AllArgsConstructor
public class Lobby implements Serializable {

    @Id
    private String id;

    @Version
    private int version;

    private String name;

    private int size;

    private Player owner;

    private Set<Player> players;

    private GameCategory gameCategory;

    private GameMode gameMode;

    private List<String> categories;

    private Game game;

    private LobbyStatus status;

    @TimeToLive
    private Long timeout;
}
