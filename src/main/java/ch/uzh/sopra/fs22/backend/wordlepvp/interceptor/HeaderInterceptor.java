package ch.uzh.sopra.fs22.backend.wordlepvp.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class HeaderInterceptor implements WebGraphQlInterceptor {

    private final Logger log = LoggerFactory.getLogger(HeaderInterceptor.class);

    @Override
    @SuppressWarnings("NullableProblems")
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, WebGraphQlInterceptor.Chain chain) {
        log.debug("Got Authorization header: {}", request.getHeaders().getFirst("Authorization"));
        return chain.next(request)
                .map(response -> {
                    response.getResponseHeaders().setBearerAuth("deadbeef-beef-dead-deadbeefdead");
                    log.debug("Set Authorization header: {}", response.getResponseHeaders().getFirst("Authorization"));
                    return response;
                });
    }
}
