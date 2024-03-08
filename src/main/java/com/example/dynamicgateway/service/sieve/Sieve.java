package com.example.dynamicgateway.service.sieve;

/**
 * In effect, a glorified {@code Predicate} that serves to filter out unwanted elements
 */
public interface Sieve<T> {
    boolean isAllowed(T element);
}
