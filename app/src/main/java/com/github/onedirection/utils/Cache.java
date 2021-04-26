package com.github.onedirection.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A simple cache that will call the provided function to generate elements,
 * and keep maxHistory elements in a map.
 * get(k) is O(backing) if k not computed yet, O(1) otherwise.
 * Note that the cache does not keep null values.
 *
 * getFunction is the function called to get elements,
 * setFunction is called when setting elements through the cache.
 * It is imperative that setFunction(k, v); getFunction(k) == v
 * since in set(), the cache can remember (k, v) without calling
 * getFunction afterwards.
 *
 * If not specified, the default setFunction will throw a RuntimeException
 * and make the cache read-only.
 *
 * @param <K> The type of the keys
 * @param <V> The type of the values
 */
public class Cache<K, V> {

    public static final int MAX_HISTORY_DEFAULT = 32;

    private final int maxHistory;
    private final Function<? super K, ? extends V> getFunction;
    private final BiFunction<? super K, ? super V, Boolean> setFunction;
    private final Map<K, V> map = new HashMap<>();
    private final LinkedBlockingQueue<K> history;

    public Cache(
            Function<? super K, ? extends V> getFunction,
            BiFunction<? super K, ? super V, Boolean> setFunction,
            int maxHistory)
    {
        // avoid edge case where functions expect a non empty cache
        if (maxHistory < 1) throw new IllegalArgumentException();
        this.getFunction = Objects.requireNonNull(getFunction);
        this.setFunction = Objects.requireNonNull(setFunction);
        this.maxHistory = maxHistory;
        this.history = new LinkedBlockingQueue<>(maxHistory);
    }

    public Cache(
            Function<? super K, ? extends V> getFunction,
            BiFunction<? super K, ? super V, Boolean> setFunction)
    {
        this(getFunction, setFunction, MAX_HISTORY_DEFAULT);
    }

    public Cache(Function<? super K, ? extends V> getFunction, int maxHistory) {
        this(getFunction, (k, v) -> { throw new RuntimeException("Unreachable, set cache, k: " + k + ", v: " + v); }, maxHistory);
    }

    public Cache(Function<? super K, ? extends V> getFunction) {
        this(getFunction, (k, v) -> { throw new RuntimeException("Unreachable, set cache, k: " + k + ", v: " + v); }, MAX_HISTORY_DEFAULT);
    }

    public Cache(int maxHistory) {
        this(k -> { throw new RuntimeException("Unreachable, get cache, k: " + k); }, maxHistory);
    }

    public Cache() {
        this(k -> { throw new RuntimeException("Unreachable, get cache, k: " + k); });
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void invalidate() {
        map.clear();
        history.clear();
    }
    
    public void invalidate(K key) {
        map.remove(key);
        // Do we actually want to suffer a fat O(n) cost here? maxHistory can be very large.
        // We could not remove the key from the history, and when evicting keys, check if the
        // removal failed (key was invalidated) and move on.
        // Problems could happen because in this case, the number of useful history slots
        // can only be at max maxHistory - #invalidate, which may be bad (force a lot of
        // undue evictions).

        //history.remove(key);
    }

    public V get(K key) {
        return get(key, getFunction);
    }

    public V get(K key, Function<? super K, ? extends V> f) {
        if (map.containsKey(key)) {
            return map.get(key);
        }

        V res = f.apply(key);
        if (res == null) {
            // Don't store null: map and queue don't accept null keys
            // Should it be an exception? In case of a backing function that
            // returns mostly null values, performance would suffer.
            return null;
        }

        putInMap(key, res);

        return res;
    }

    private void putInMap(K key, V value) {
        map.put(key, value);

        if (!history.offer(key)) {
            // the history is full
            K oldest;
            // while because invalidated may still be in the history and need to be skipped
            do {
                oldest = history.remove();
            } while (map.remove(oldest) == null);
            history.offer(key);
        }
    }

    public boolean set(K key, V value) {
        return set(key, value, setFunction, true);
    }

    public boolean set(K key, V value, BiFunction<? super K, ? super V, Boolean> f, boolean writeAllocate) {
        boolean wasInserted = f.apply(key, value);
        if (wasInserted && writeAllocate) {
            putInMap(key, value);
        }
        return wasInserted;
    }

    public Map<K, V> getMap() {
        return Collections.unmodifiableMap(map);
    }
}
