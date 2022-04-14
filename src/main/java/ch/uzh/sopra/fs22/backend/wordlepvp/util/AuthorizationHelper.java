package ch.uzh.sopra.fs22.backend.wordlepvp.util;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class AuthorizationHelper {
    public static String extractAuthToken(@NonNull String authHeader) {
        if (authHeader.isEmpty())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You have to log in to access this resource.");
        return authHeader.replace("Bearer ", "");
    }
}
