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
 * getFunction is the default function called to get elements,
 * setFunction is the default function called when setting elements through the cache.
 * It is imperative that setFunction(k, v); getFunction(k) == v
 * since in set(), the cache can remember (k, v) without calling
 * getFunction afterwards.
 *
 * If not specified, the default get/setFunction will throw a RuntimeException
 * and thus you will have to use the overloads of get and set that can accept a function.
 *
 * @param <K> The type of the keys
 * @param <V> The type of the values
 */
public class Cache<K, V> {

    public static final int MAX_HISTORY_DEFAULT = 1024;

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
        this(getFunction, (k, v) -> { throw new RuntimeException("No default setFunction provided. k: " + k + ", v: " + v); }, maxHistory);
    }

    public Cache(Function<? super K, ? extends V> getFunction) {
        this(getFunction, (k, v) -> { throw new RuntimeException("No default setFunction provided. k: " + k + ", v: " + v); }, MAX_HISTORY_DEFAULT);
    }

    public Cache(int maxHistory) {
        this(k -> { throw new RuntimeException("No default getFunction provided. k: " + k); }, maxHistory);
    }

    public Cache() {
        this(k -> { throw new RuntimeException("No default getFunction provided. k: " + k); });
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
    }

    /**
     * Same as get(k, f) but using the default getFunction.
     * @param key
     * @return
     */
    public V get(K key) {
        return get(key, getFunction);
    }

    /**
     * Get the value at key and use the function f to get it if it wasn't already in the cache.
     * If the value corresponding to key is null, it is not stored in the cache.
     * Complexity O(F) where F is the complexity of f.
     * @param key
     * @param f The function called if key is not in the cache.
     * @return The value corresponding to key.
     */
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

    /**
     * Same as set(k, v, wa, f) but using the default setFunction and writeAllocate = true.
     * @param key
     * @param value
     * @return
     */
    public boolean set(K key, V value) {
        return set(key, value, setFunction, true);
    }

    /**
     * Same as set(k, v, wa, f) but using the default setFunction.
     * @param key
     * @param value
     * @param writeAllocate
     * @return
     */
    public boolean set(K key, V value, boolean writeAllocate)  {
        return set(key, value, setFunction, writeAllocate);
    }

    /**
     * Assign a value to a key in the cache. The function passed as argument is called with the provided
     * key and value, and is supposed to insert the same k,v pair into the cache's backing store.
     * If f doesn't propagate the set() and ignores it, the cache may become inconsistent.
     * @param key
     * @param value
     * @param f
     * @param writeAllocate If true, the cache will also immediatly store the value. If false, it will only invalidate the cache at key.
     * @return
     */
    public boolean set(K key, V value, BiFunction<? super K, ? super V, Boolean> f, boolean writeAllocate) {
        boolean wasInserted = f.apply(key, value);
        if (wasInserted) {
            if (writeAllocate) {
                putInMap(key, value);
            } else {
                invalidate(key);
            }
        }
        return wasInserted;
    }

    public Map<K, V> getMap() {
        return Collections.unmodifiableMap(map);
    }
}
