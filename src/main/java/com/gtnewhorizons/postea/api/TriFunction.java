package com.gtnewhorizons.postea.api;

/**
 * A simple function interface for functions with 3 parameters that must return a value.
 *
 * @param <T> The first parameter type.
 * @param <U> The second parameter type.
 * @param <O> The third parameter type.
 * @param <R> The return type.
 */
@FunctionalInterface
public interface TriFunction<T, U, O, R> {

    R apply(T t, U u, O o);
}
