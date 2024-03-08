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

    public static Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, Throwable throwable) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.writeWith(Mono.just(createErrorBody(throwable.getMessage())));
    }

    private static DataBuffer createErrorBody(String errorBody) {
        byte[] bytes = errorBody.getBytes(StandardCharsets.UTF_8);
        return DefaultDataBufferFactory.sharedInstance.wrap(bytes);
    }
}
