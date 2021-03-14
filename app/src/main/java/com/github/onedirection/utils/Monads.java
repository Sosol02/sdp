package com.github.onedirection.utils;

import com.google.android.gms.tasks.Task;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Functions to manipulate or transform monadic classes.  */
public final class Monads {

    private Monads() {}

    public static <T> CompletableFuture<T> flatten(CompletableFuture<Optional<T>> m){
        return m.thenCompose(o -> toFuture(o));
    }

    public static <T> CompletableFuture<T> toFuture(Task<T> m){
        CompletableFuture<T> result = new CompletableFuture<>();
        m.addOnSuccessListener(r -> result.complete(r))
            .addOnFailureListener(t -> result.completeExceptionally(t))
            .addOnCanceledListener(() -> result.cancel(true));
        return result;
    }

    public static <T> CompletableFuture<T> toFuture(Optional<T> m){
        return CompletableFuture.supplyAsync(() -> m.get());
    }
}
