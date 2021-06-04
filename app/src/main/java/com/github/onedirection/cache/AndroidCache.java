package com.github.onedirection.cache;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utils to use the cache classes with the actual cache directory of Android.
 */
public final class AndroidCache {

    private AndroidCache() {
    }

    public static <K, V, KS extends Serializable, VS extends Serializable>
    boolean dumpToAndroidCache(
            Context ctx,
            String cacheName,
            Cache<K, V> cache,
            Function<K, KS> keyMap,
            Function<V, VS> valMap
    ) {
        File file = new File(ctx.getExternalCacheDir(), cacheName);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            return cache.dumpToStream(outputStream, keyMap, valMap);
        } catch (IOException e) {
            return false;
        }
    }

    public static <K, V, KS extends Serializable, VS extends Serializable>
    Optional<Cache<K, V>> loadFromAndroidCache(
            Context ctx,
            String cacheName,
            Function<? super K, ? extends V> getFunction,
            BiFunction<? super K, ? super V, Boolean> setFunction,
            Function<KS, K> keyMap,
            Function<VS, V> valMap
    ) {
        File file = new File(ctx.getExternalCacheDir(), cacheName);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return Cache.loadFromStream(inputStream, getFunction, setFunction, keyMap, valMap);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static <K, V, KS extends Serializable, VS extends Serializable>
    Optional<Cache<K, V>> loadFromAndroidCache(
            Context ctx,
            String cacheName,
            Function<? super K, ? extends V> getFunction,
            Function<KS, K> keyMap,
            Function<VS, V> valMap
    ) {
        return loadFromAndroidCache(ctx, cacheName, getFunction, Cache.defaultSetFunction(), keyMap, valMap);
    }

}
