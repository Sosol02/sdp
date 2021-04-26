package com.github.onedirection.geolocation.geocoding;

import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.utils.Cache;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class GeocodingCache implements GeocodingService {
    private static class Key{
        public final String str;
        public final int i;

        public Key(String str, int i){
            this.str = str;
            this.i = i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key stringInt = (Key) o;
            return i == stringInt.i &&
                    Objects.equals(str, stringInt.str);
        }

        @Override
        public int hashCode() {
            return Objects.hash(str, i);
        }
    }

    private final Cache<Key, CompletableFuture<List<NamedCoordinates>>> nameCache;
    private final Cache<Coordinates, CompletableFuture<NamedCoordinates>> coordinatesCache;

    public GeocodingCache(GeocodingService underlying){
        nameCache = new Cache<>(
                key -> underlying.getNamedCoordinates(key.str, key.i)
        );
        coordinatesCache = new Cache<>(
                coordinates -> underlying.getBestNamedCoordinates(coordinates)
        );
    }

    @Override
    public CompletableFuture<List<NamedCoordinates>> getNamedCoordinates(String locationName, int count) {
        return nameCache.get(new Key(locationName, count));
    }

    @Override
    public CompletableFuture<NamedCoordinates> getBestNamedCoordinates(Coordinates coordinates) {
        return coordinatesCache.get(coordinates);
    }
}
