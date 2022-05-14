package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@Data
@Builder
@RedisHash
@NoArgsConstructor
@AllArgsConstructor
public class LobbyInvite implements Serializable {
    @Id
    private String id;

    @Version
    private int version;

    private String lobbyId;

    private String senderId;

    private String recipientId;

    @TimeToLive
    private Long timeout;
}
