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
@Entity
@Builder
@RedisHash
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lobbies")
public class Lobby implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column(nullable = false, updatable = false)
    //TODO: Should be HashID
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int size;

    @Column(nullable = false)
    private UUID owner;

    @OneToMany
    private Set<User> players;

    @Column(nullable = false)
    private GameCategory gameCategory;

    @Column(nullable = false)
    private GameMode gameMode;

    @Column(nullable = false)
    //TODO: implement Game class (this should be a Game object)
    private String game;

    //TODO: should be JSON
    @Column(nullable = false)
    private String settings;

    @Column(nullable = false)
    private LobbyStatus status;
}
