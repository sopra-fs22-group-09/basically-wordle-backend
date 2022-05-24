package ch.uzh.sopra.fs22.backend.wordlepvp.util;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.NonNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Component
public class GraphQLErrorResolver extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        Map<String, Object> ext = new HashMap<>();
        ErrorType errType = ErrorType.INTERNAL_ERROR;
        if (ex instanceof ResponseStatusException) {
            switch (((ResponseStatusException) ex).getStatus()) {
                case BAD_REQUEST -> errType = ErrorType.BAD_REQUEST;
                case UNAUTHORIZED -> errType = ErrorType.UNAUTHORIZED;
                case FORBIDDEN -> errType = ErrorType.FORBIDDEN;
                case NOT_FOUND -> errType = ErrorType.NOT_FOUND;
                default -> {
                }
            }
        }
        ext.put("code", errType.toString());
        return GraphqlErrorBuilder.newError(env)
                .message(ex.getMessage())
                .extensions(ext)
                .errorType(errType)
                .build();
    }
}
