package com.github.onedirection.utils;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

/**
 * A simple cache that will call the provided function to generate elements,
 * and keep maxHistory elements in a map.
 * get(k) is O(backing) if k not computed yet, O(1) otherwise.
 * Note that the cache does not keep null values
 * @param <K> The type of the keys
 * @param <V> The type of the values
 */
public class Cache<K, V> {

    private static final int MAX_HISTORY_DEFAULT = 32;

    private final int maxHistory;
    private final Function<? super K, ? extends V> backing;
    private final Map<K, V> map = new HashMap<>();
    private final LinkedBlockingQueue<K> history;

    public Cache(Function<? super K, ? extends V> backing) {
        this.backing = backing;
        this.maxHistory = MAX_HISTORY_DEFAULT;
        this.history = new LinkedBlockingQueue<>(maxHistory);
    }

    public Cache(Function<? super K, ? extends V> backing, int maxHistory) {
        // avoid edge case where functions expect a non empty cache
        if (maxHistory < 1) throw new IllegalArgumentException();
        this.backing = backing;
        this.maxHistory = maxHistory;
        this.history = new LinkedBlockingQueue<>(maxHistory);
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public V get(K key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }

        V res = backing.apply(key);
        if (res == null) {
            // Don't store null: map and queue don't accept null keys
            // Should it be an exception? In case of a backing function that
            // returns mostly null values, performance would suffer.
            return null;
        }

        map.put(key, res);

        if (!history.offer(key)) {
            // the history is full
            K oldest = history.remove();
            map.remove(oldest);
            history.offer(key);
        }

        return res;
    }

    public Map<K, V> getMap() {
        return Collections.unmodifiableMap(map);
    }
}
