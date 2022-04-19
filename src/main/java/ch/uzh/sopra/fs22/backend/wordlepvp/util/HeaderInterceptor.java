package ch.uzh.sopra.fs22.backend.wordlepvp.util;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.server.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Component
public class HeaderInterceptor implements WebSocketGraphQlInterceptor {

    private final Logger log = LoggerFactory.getLogger(HeaderInterceptor.class);

    private final UserService userService;

    public HeaderInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, WebGraphQlInterceptor.Chain chain) {
        String authHeader;

        if (request instanceof WebSocketGraphQlRequest) {
            authHeader = String.valueOf(((WebSocketGraphQlRequest) request).getSessionInfo().getAttributes().getOrDefault("Authorization", "null"));
        } else {
            authHeader = request.getHeaders().getFirst("Authorization");
        }
        log.debug("Got Authorization header: {}", authHeader);
        if (authHeader != null && !authHeader.equals("null"))
            request.configureExecutionInput((executionInput, builder) ->
                    builder.graphQLContext(Collections.singletonMap("Authorization", authHeader)).build());

        return chain.next(request).publishOn(Schedulers.boundedElastic()).mapNotNull(response -> {
            if (!response.getExecutionResult().isDataPresent() || !response.isValid()) return response;
            ObjectMapper oMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            if (response.getExecutionResult().isDataPresent()) {
            try {
                /* FIXME: class graphql.execution.reactive.SubscriptionPublisher cannot be cast to class
                   java.util.Map (graphql.execution.reactive.SubscriptionPublisher is in unnamed module of loader 'app';
                   java.util.Map is in module java.base of loader 'bootstrap') */
                Map<String, User> map = response.getData();
                User user = null;
                if (map == null) return response;
                if (map.containsKey("register")) {
                    user = oMapper.convertValue(map.get("register"), User.class);
                } else if (map.containsKey("login")) {
                    user = oMapper.convertValue(map.get("login"), User.class);
                }
                if (user == null) return response;
                String bearerToken = this.userService.authorize(user);
                response.getResponseHeaders().setBearerAuth(bearerToken);
                log.debug("Set Authorization header: {}", response.getResponseHeaders().getFirst("Authorization"));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            return response;
        });
    }

    // ONLY FOR WEBSOCKET CONNECTIONS!

    @Override
    public @NonNull Mono<Object> handleConnectionInitialization(@NonNull WebSocketSessionInfo sessionInfo, @NonNull Map<String, Object> connectionInitPayload) {
        // FIXME: This seems fishy and should be gracefully handled!
        String authHeader = String.valueOf(connectionInitPayload.get("Authorization"));
        if (!Objects.equals(authHeader, "null")) {
            sessionInfo.getAttributes().put("Authorization", authHeader);
        }
        return WebSocketGraphQlInterceptor.super.handleConnectionInitialization(sessionInfo, connectionInitPayload);
    }
}
