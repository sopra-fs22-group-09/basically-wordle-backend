package ch.uzh.sopra.fs22.backend.wordlepvp.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

public class HeaderInterceptor implements WebGraphQlInterceptor {

    private final Logger log = LoggerFactory.getLogger(HeaderInterceptor.class);

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        return chain.next(request)
                .map(response -> {
                    Object data = response.getData();
                    Object updatedData = data;
                    log.debug(data.toString());
                            //response.getResponseHeaders();
                    return response.transform(builder -> builder.data(updatedData));
                });
    }
}
