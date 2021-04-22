package com.github.onedirection;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

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
