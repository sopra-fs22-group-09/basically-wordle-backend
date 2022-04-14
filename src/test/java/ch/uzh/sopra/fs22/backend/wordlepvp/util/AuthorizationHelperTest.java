package ch.uzh.sopra.fs22.backend.wordlepvp.util;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationHelperTest {

    @Test
    void extractAuthTokenStringNull() {
        String authHeader = null;

        assertThrows(NullPointerException.class, () -> {
            AuthorizationHelper.extractAuthToken(authHeader);
        }, "Null token should have thrown an Exception.");
    }

    @Test
    void extractAuthTokenStringEmpty() {
        String authHeader = "";

        assertThrows(ResponseStatusException.class, () -> {
            AuthorizationHelper.extractAuthToken(authHeader);
        }, "Empty token should have thrown an Exception.");
    }

    @Test
    void extractAuthToken() {
        String authHeader = "Bearer deadbeef-dead-beef-cafe-deadbeefcafe";
        String expected = "deadbeef-dead-beef-cafe-deadbeefcafe";

        String actual = AuthorizationHelper.extractAuthToken(authHeader);

        assertEquals(expected, actual, "Authorization token does not match.");
    }
}