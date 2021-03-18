package com.github.onedirection.utils;

import java.util.Objects;

public final class Pair<S, T> {
    public final S first;
    public final T second;

    public Pair(S first, T second){
        this.first = first;
        this.second = second;
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
}
