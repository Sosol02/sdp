package com.github.onedirection.geolocation.geocoding;

import android.content.Context;

import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.geolocation.model.NamedCoordinates;
import com.github.onedirection.cache.AndroidCache;
import com.github.onedirection.cache.Cache;
import com.github.onedirection.utils.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A wrapper for geocoding providing caching.
 */
public final class GeocodingCache implements GeocodingService {
    /**
     * Note: As you will see, this class has a particular (unexpected) way
     * of dealing with the number of results. This is so due to the Cache
     * interface, which wasn't designed for this kind of special cases.
     * <p>
     * BUT geocoding will usually always be used with the same number of results,
     * so this shouldn't be a real issue.
     */

    private static <T extends Serializable> ArrayList<T> dumpList(CompletableFuture<List<T>> cf) {
        return cf.thenApply(ArrayList::new).getNow(null);
    }

    private static <T extends Serializable> CompletableFuture<List<T>> loadList(ArrayList<T> val) {
        return CompletableFuture.completedFuture(Collections.unmodifiableList(val));
    }

    private final Cache<Pair<String, Integer>, CompletableFuture<List<NamedCoordinates>>> nameCache;
    private final Cache<Coordinates, CompletableFuture<NamedCoordinates>> coordinatesCache;

    private GeocodingCache(
            Cache<Pair<String, Integer>, CompletableFuture<List<NamedCoordinates>>> nameCache,
            Cache<Coordinates, CompletableFuture<NamedCoordinates>> coordinatesCache
    ) {
        this.nameCache = nameCache;
        this.coordinatesCache = coordinatesCache;
    }

    public GeocodingCache(GeocodingService underlying) {
        this(
                new Cache<>(
                        key -> underlying.getNamedCoordinates(key.first, key.second)
                ),
                new Cache<>(
                        underlying::getBestNamedCoordinates
                )
        );
        Objects.requireNonNull(underlying);
    }

    @Override
    public CompletableFuture<List<NamedCoordinates>> getNamedCoordinates(String locationName, int count) {
        return nameCache.get(new Pair<>(locationName, count));
    }

    @Override
    public CompletableFuture<NamedCoordinates> getBestNamedCoordinates(Coordinates coordinates) {
        return coordinatesCache.get(coordinates);
    }

    public boolean dumpToAndroidCache(Context ctx, String filename) {
        return AndroidCache.dumpToAndroidCache(ctx, filename + "_1", nameCache, x -> x, GeocodingCache::dumpList)
                && AndroidCache.dumpToAndroidCache(ctx, filename + "_2", coordinatesCache, x -> x, cf -> cf.getNow(null));
    }

    public static Optional<GeocodingCache> loadFromAndroidCache(Context ctx, String filename, GeocodingService underlying) {
        Objects.requireNonNull(underlying);
        Optional<Cache<Pair<String, Integer>, CompletableFuture<List<NamedCoordinates>>>> nameCache = AndroidCache.loadFromAndroidCache(
                ctx,
                filename + "_1",
                key -> underlying.getNamedCoordinates(key.first, key.second),
                (Pair<String, Integer> x) -> x,
                (Function<ArrayList<NamedCoordinates>, CompletableFuture<List<NamedCoordinates>>>) GeocodingCache::loadList
        );
        Optional<Cache<Coordinates, CompletableFuture<NamedCoordinates>>> coordinatesCache = AndroidCache.loadFromAndroidCache(
                ctx,
                filename + "_2",
                underlying::getBestNamedCoordinates,
                (Coordinates x) -> x,
                (Function<NamedCoordinates, CompletableFuture<NamedCoordinates>>) CompletableFuture::completedFuture
        );

        if(nameCache.isPresent() && coordinatesCache.isPresent()){
            return Optional.of(new GeocodingCache(nameCache.get(), coordinatesCache.get()));
        }
        else{
            return Optional.empty();
        }
    }
}
