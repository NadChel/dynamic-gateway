package com.example.dynamicgateway.util;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class ResponseWriter {
    private ResponseWriter() {
    }

    /**
     * Gets the exchange's response and:
     * <p>
     * 1. Sets its status to {@code 401 Unauthorized}
     * <p>
     * 2. Writes the throwable's detail message (retrieved by calling
     * {@link Throwable#getMessage()}) to its body
     * <p>
     * When converting the detail message string to a byte array, this method
     * passes {@link StandardCharsets#UTF_8}
     *
     * @param exchange exchange whose response should be mutated
     * @param throwable throwable that supplies a detail message to write to
     *                  the exchange's response
     * @return empty {@code Mono} on successful completion
     */
    public static Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, Throwable throwable) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        DataBuffer responseBuffer = createBufferFrom(throwable.getMessage());
        return response.writeWith(Mono.just(responseBuffer));
    }

    private static DataBuffer createBufferFrom(String errorBody) {
        byte[] bytes = errorBody.getBytes(StandardCharsets.UTF_8);
        return DefaultDataBufferFactory.sharedInstance.wrap(bytes);
    }
}
