package com.example.dynamicgateway.service.sieve;

import java.util.function.Predicate;

/**
 * A functional equivalent of {@link Predicate} that serves to filter out unwanted elements
 *
 * @param <T> type of screened elements
 */
@FunctionalInterface
public interface Sieve<T> {
    /**
     * Tests whether an element should be kept
     *
     * @param element element to test
     * @return {@code true} if the element should be retained,
     * {@code false} otherwise
     */
    boolean isAllowed(T element);
}
