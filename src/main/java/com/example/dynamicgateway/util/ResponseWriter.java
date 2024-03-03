package com.example.dynamicgateway.util;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ResponseWriter {
    public static Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, Throwable throwable) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.writeWith(Mono.just(createErrorBody(throwable.getMessage())));
    }

    private static DataBuffer createErrorBody(String errorBody) {
        byte[] bytes = errorBody.getBytes();
        return DefaultDataBufferFactory.sharedInstance.wrap(bytes);
    }
}
