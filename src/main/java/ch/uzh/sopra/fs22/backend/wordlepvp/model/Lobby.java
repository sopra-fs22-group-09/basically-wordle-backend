package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Data
@Builder
@ToString
//@RedisHash("lobbies")
@NoArgsConstructor
@AllArgsConstructor
public class Lobby implements Serializable {

    @Id
    //TODO: Should be HashID
    private String id;

    @Version
    private int version;

    private String name;

    private int size;

    private User owner;

    private Set<User> players;

    private GameCategory gameCategory;

    private GameMode gameMode;

    //TODO: implement Game class (this should be a Game object)
    private String game;

    //TODO: should be JSON
    private String settings;

    private LobbyStatus status;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastUpdatedDate;
}
