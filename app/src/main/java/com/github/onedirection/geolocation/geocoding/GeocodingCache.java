package com.github.onedirection.geolocation.geocoding;

import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.utils.Cache;
import com.github.onedirection.utils.Pair;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**  A wrapper for geocoding providing caching. */
public final class GeocodingCache implements GeocodingService {
    /**
     * Note: As you will see, this class has a particular (unexpected) way
     * of dealing with the number of results. This is so due to the Cache
     * interface, which wasn't designed for this kind of special cases.
     *
     * BUT geocoding will usually always be used with the same number of results,
     * so this shouldn't be a real issue.
     */

    private final Cache<Pair<String, Integer>, CompletableFuture<List<NamedCoordinates>>> nameCache;
    private final Cache<Coordinates, CompletableFuture<NamedCoordinates>> coordinatesCache;

    public GeocodingCache(GeocodingService underlying){
        nameCache = new Cache<>(
                key -> underlying.getNamedCoordinates(key.first, key.second)
        );
        coordinatesCache = new Cache<>(
                underlying::getBestNamedCoordinates
        );
    }

    @Override
    public CompletableFuture<List<NamedCoordinates>> getNamedCoordinates(String locationName, int count) {
        return nameCache.get(new Pair<>(locationName, count));
    }

    @Override
    public CompletableFuture<NamedCoordinates> getBestNamedCoordinates(Coordinates coordinates) {
        return coordinatesCache.get(coordinates);
    }
}
