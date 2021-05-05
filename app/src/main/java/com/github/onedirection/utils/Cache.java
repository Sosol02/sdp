package com.github.onedirection.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

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

    public static final int MAX_HISTORY_DEFAULT = 32;

    private final int maxHistory;
    private final Function<? super K, ? extends V> getFunction;
    private final BiFunction<? super K, ? super V, Boolean> setFunction;
    private final Map<K, V> map;
    private final LinkedBlockingQueue<K> history;

    private static<K, V> BiFunction<? super K, ? super V, Boolean> defaultSetFunction() {
        return (k, v) -> { throw new RuntimeException("No default setFunction provided. k: " + k + ", v: " + v); };
    }

    private static<K, V> Function<? super K, ? extends V> defaultGetFunction() {
        return k -> { throw new RuntimeException("No default getFunction provided. k: " + k); };
    }

    private Cache(
            Function<? super K, ? extends V> getFunction,
            BiFunction<? super K, ? super V, Boolean> setFunction,
            int maxHistory,
            LinkedBlockingQueue<K> history,
            Map<K, V> map
    ) {
        // avoid edge case where functions expect a non empty cache
        if (maxHistory < 1) throw new IllegalArgumentException();
        this.getFunction = Objects.requireNonNull(getFunction);
        this.setFunction = Objects.requireNonNull(setFunction);
        this.maxHistory = maxHistory;
        this.map = map;
        this.history = history;

        // Note: I did not a find a way to verify that the capacity of the queue
        // is indeed maxHistory...
    }

    public Cache(
            Function<? super K, ? extends V> getFunction,
            BiFunction<? super K, ? super V, Boolean> setFunction,
            int maxHistory)
    {
        this(getFunction, setFunction, maxHistory, new LinkedBlockingQueue<>(maxHistory), new HashMap<>());
    }

    public Cache(
            Function<? super K, ? extends V> getFunction,
            BiFunction<? super K, ? super V, Boolean> setFunction)
    {
        this(getFunction, setFunction, MAX_HISTORY_DEFAULT);
    }

    public Cache(Function<? super K, ? extends V> getFunction, int maxHistory) {
        this(getFunction, defaultSetFunction(), maxHistory);
    }

    public Cache(Function<? super K, ? extends V> getFunction) {
        this(getFunction, defaultSetFunction(), MAX_HISTORY_DEFAULT);
    }

    public Cache(int maxHistory) {
        this(defaultGetFunction(), maxHistory);
    }

    public Cache() {
        this(defaultGetFunction());
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

    public boolean isCached(K key){
        return map.containsKey(key);
    }

    public Map<K, V> getMap() {
        return Collections.unmodifiableMap(map);
    }



    ///////////////////////////////////////
    //           Persistence             //
    ///////////////////////////////////////

    private static <K1, V1, K2, V2> BiFunction<K1, V1, Map.Entry<K2, V2>> combine(Function<K1, K2> keyMap, Function<V1, V2> valMap) {
        return (k, v) -> Pair.of(keyMap.apply(k), valMap.apply(v)).toEntry();
    }

    /**
     * Serialize this cache into a stream.
     *
     * Note that the get/set functions of the cache cannot be serialized and must
     * be specified when de-serializing to obtain the exact same cache.
     * @param target The stream where to serialize.
     * @param keyMap Convert the keys into a serializable type.
     * @param valMap Convert the values into a serializable type.
     * @param <KS> The serializable key type.
     * @param <VS> The serializable value type.
     * @return True if serialization succeeded, false otherwise.
     */
    public <KS extends Serializable, VS extends Serializable>
    boolean dumpToStream(
            OutputStream target,
            Function<K, KS> keyMap,
            Function<V, VS> valMap
    ){
        try(ObjectOutputStream outputStream = new ObjectOutputStream(target)) {
            outputStream.writeInt(maxHistory);
            outputStream.writeObject(Monads.map(map, combine(keyMap, valMap)));
            outputStream.writeObject( history.stream().map(keyMap).collect(toList()) );

            outputStream.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Create a cache from a serialized version.
     * @param src The stream containing the serialized version.
     * @param getFunction The default get function the cache will use.
     * @param setFunction The default set function the cache will use.
     * @param keyMap Convert the serialized keys to keys.
     * @param valMap Convert the serialized values to values.
     * @param <K> The "key" type of the cache.
     * @param <V> The "value" type of the cache.
     * @param <KS> The type of the serialized keys.
     * @param <VS> The type of the serialized values.
     * @return The cache if de-serialization succeeded.
     */
    public static<K, V, KS extends Serializable, VS extends Serializable>
    Optional<Cache<K, V>> loadFromStream(
            InputStream src,
            Function<? super K, ? extends V> getFunction,
            BiFunction<? super K, ? super V, Boolean> setFunction,
            Function<KS, K> keyMap,
            Function<VS, V> valMap
    ){
        try(ObjectInputStream inputStream = new ObjectInputStream(src)){
            int maxHistory = inputStream.readInt();
            @SuppressWarnings("unchecked")
            Map<KS, VS> rawMap = (Map<KS, VS>) inputStream.readObject();
            @SuppressWarnings("unchecked")
            List<KS> rawHistory = (List<KS>) inputStream.readObject();

            LinkedBlockingQueue<K> history = new LinkedBlockingQueue<>(maxHistory);
            history.addAll( Monads.map(rawHistory, keyMap) );
            Map<K, V> map = Monads.map(rawMap, combine(keyMap, valMap));

            return Optional.of(new Cache<K, V>(
                    getFunction,
                    setFunction,
                    maxHistory,
                    history,
                    map
            ));
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Same as the other version, but generate a cache without default set function.
     */
    public static<K, V, KS extends Serializable, VS extends Serializable>
    Optional<Cache<K, V>> loadFromStream(
            InputStream src,
            Function<? super K, ? extends V> getFunction,
            Function<KS, K> keyMap,
            Function<VS, V> valMap
    ){
        return loadFromStream(src, getFunction, defaultSetFunction(), keyMap, valMap);
    }
}
