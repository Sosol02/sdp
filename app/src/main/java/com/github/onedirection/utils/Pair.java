package com.github.onedirection.utils;

import java.util.Map;
import java.util.Objects;

public final class Pair<S, T> {
    public final S first;
    public final T second;

    public Pair(S first, T second){
        this.first = first;
        this.second = second;
    }

    public static<S, T> Pair<S, T> of(S first, T second){
        return new Pair(first, second);
    }

    @Override
    public String toString() {
        return "Pair{" +
                first +
                ", " + second +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    public Map.Entry<S, T> toEntry() {
        return new Map.Entry<S, T>() {
            @Override
            public S getKey() {
                return first;
            }

            @Override
            public T getValue() {
                return second;
            }

            @Override
            public T setValue(T value) {
                throw new UnsupportedOperationException("This entry is backed by a Pair");
            }
        };
    }
}
