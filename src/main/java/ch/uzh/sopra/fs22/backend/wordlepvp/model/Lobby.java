package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.util.Set;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@RedisHash
@NoArgsConstructor
@AllArgsConstructor
public class Lobby implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    //TODO: Should be HashID
    private Long id;

    private String name;

    private int size;

    private UUID owner;

    private Set<User> players;

    private GameCategory gameCategory;

    private GameMode gameMode;

    //TODO: implement Game class (this should be a Game object)
    private String game;

    //TODO: should be JSON
    private String settings;

    private LobbyStatus status;
}
