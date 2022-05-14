package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
//@RedisHash("lobbies")
@NoArgsConstructor
@AllArgsConstructor
public class Lobby implements Serializable {

    //TODO: Should be HashID ?
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

    private Game game;

    private LobbyStatus status;

    @TimeToLive
    private Long timeout;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastUpdatedDate;
}
