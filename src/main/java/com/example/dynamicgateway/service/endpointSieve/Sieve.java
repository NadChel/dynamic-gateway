package com.example.dynamicgateway.service.endpointSieve;

/**
 * In effect, a glorified {@code Predicate} that serves to filter out unwanted elements
 */
public interface Sieve<T> {
    boolean isAllowed(T element);
}