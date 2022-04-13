package ch.uzh.sopra.fs22.backend.wordlepvp.util;

import org.springframework.web.server.ServerWebExchange;
import reactor.util.context.Context;

import java.util.List;
import java.util.Optional;

public final class AuthorizationHelper {
    public static String getAuthTokenFromContext(Context ctx) {
        List<String> authHeader = null;
        Optional<ServerWebExchange> swe = ctx.getOrEmpty(ServerWebExchange.class);
        if (swe.isPresent())
            authHeader = swe.get().getRequest().getHeaders().get("authorization");
        if (authHeader == null || authHeader.isEmpty()) return null;
        return authHeader.get(0).replace("Bearer ", "");
    }
}
