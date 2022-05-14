package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@Builder
@RedisHash("players")
@NoArgsConstructor
@AllArgsConstructor
public class Player implements Serializable {

    @Id
    private String id;

    private String name;

    private String avatarID;

    private String lobbyId;

}