package com.example.dynamicgateway.service.routeLocator.util;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.http.server.RequestPath;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class PathOnlyAsyncPredicate implements AsyncPredicate<ServerWebExchange> {
    private final RequestPath path;

    private PathOnlyAsyncPredicate(String path) {
        this.path = RequestPath.parse(path, "");
    }

    public static AsyncPredicate<ServerWebExchange> from(String path) {
        return new PathOnlyAsyncPredicate(path);
    }

    @Override
    public Publisher<Boolean> apply(ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getPath().equals(path));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathOnlyAsyncPredicate that = (PathOnlyAsyncPredicate) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
