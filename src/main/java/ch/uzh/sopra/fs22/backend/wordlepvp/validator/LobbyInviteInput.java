package ch.uzh.sopra.fs22.backend.wordlepvp.validator;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LobbyInviteInput {
    @NotNull
    private String recipientId;

    @NotNull
    private String lobbyId;
}
