package com.github.onedirection.utils;

import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Functions to manipulate or transform monadic classes.
 */
public final class Monads {

    private Monads() {
    }

    public static <T> CompletableFuture<T> flatten(CompletableFuture<Optional<T>> m) {
        return m.thenCompose(o -> toFuture(o));
    }

    public static <T> CompletableFuture<T> flattenFuture(CompletableFuture<CompletableFuture<T>> fut) {
        return fut.thenCompose(x -> x);
    }

    public static <T> CompletableFuture<T> toFuture(Task<T> m) {
        CompletableFuture<T> result = new CompletableFuture<>();
        m.addOnSuccessListener(result::complete)
                .addOnFailureListener(result::completeExceptionally)
                .addOnCanceledListener(() -> result.cancel(true));
        return result;
    }

    public static <T, S> List<S> map(List<T> ls, Function<T, S> transform) {
        return ls.stream().map(transform).collect(Collectors.toList());
    }

    public static <K1, V1, K2, V2> Map<K2, V2> map(Map<K1, V1> m, BiFunction<K1, V1, Map.Entry<K2, V2>> transform) {
        return m.entrySet().stream()
                .map(e -> transform.apply(e.getKey(), e.getValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <T> CompletableFuture<T> toFuture(Optional<T> m) {
        CompletableFuture<T> result = new CompletableFuture<>();
        if (m.isPresent()) {
            result.complete(m.get());
        } else {
            result.completeExceptionally(new NoSuchElementException("The provided optional was empty"));
        }
        return result;
    }
}
