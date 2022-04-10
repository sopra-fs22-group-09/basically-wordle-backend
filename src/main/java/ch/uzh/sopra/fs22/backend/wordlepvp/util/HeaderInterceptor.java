package ch.uzh.sopra.fs22.backend.wordlepvp.util;

import ch.uzh.sopra.fs22.backend.wordlepvp.model.User;
import ch.uzh.sopra.fs22.backend.wordlepvp.service.UserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Component
public class HeaderInterceptor implements WebGraphQlInterceptor {

    private final Logger log = LoggerFactory.getLogger(HeaderInterceptor.class);

    private final UserService userService;

    public HeaderInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, WebGraphQlInterceptor.Chain chain) {
        log.debug("Got Authorization header: {}", request.getHeaders().getFirst("Authorization"));
        return chain.next(request).publishOn(Schedulers.boundedElastic()).mapNotNull(response -> {
            ObjectMapper oMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                Map<String, User> mappli = response.getData();
                User user = null;
                if (mappli == null) return response;
                if (mappli.containsKey("register")) {
                    user = oMapper.convertValue(mappli.get("register"), User.class);
                } else if (mappli.containsKey("login")) {
                    user = oMapper.convertValue(mappli.get("login"), User.class);
                }
                if (user == null) return response;
                String bearerToken = userService.giveMeDaAuthToken(user.getId()).toString();
                response.getResponseHeaders().setBearerAuth(bearerToken);
                log.debug("Set Authorization header: {}", response.getResponseHeaders().getFirst("Authorization"));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            return response;
        });
    }
}
