package com.github.onedirection.geolocation;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.geolocation.geocoding.GeocodingCache;
import com.github.onedirection.geolocation.geocoding.GeocodingService;
import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.geolocation.model.NamedCoordinates;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class GeocodingCacheTest {

    private final static String FILE_NAME = "Tests";

    class DummyGeocoding implements GeocodingService {

        public int count = 0;

        @Override
        public CompletableFuture<List<NamedCoordinates>> getNamedCoordinates(String locationName, int ignored) {
            count += 1;
            return CompletableFuture.completedFuture(Collections.singletonList(new NamedCoordinates(0, 0, locationName)));
        }

        @Override
        public CompletableFuture<NamedCoordinates> getBestNamedCoordinates(Coordinates coordinates) {
            count += 1;
            return CompletableFuture.completedFuture(new NamedCoordinates(coordinates, "Nowhere"));
        }
    }

    @Test
    public void canBeDumpedAndLoaded(){
        DummyGeocoding base = new DummyGeocoding();
        GeocodingCache cache = new GeocodingCache(base);

        cache.getBestNamedCoordinates(new Coordinates(0, 0));
        cache.getNamedCoordinates("Somewhere", 1);
        cache.getBestNamedCoordinates(new Coordinates(0, 0));
        cache.getNamedCoordinates("Somewhere", 1);
        assertThat(base.count, is(2));

        cache.dumpToAndroidCache(ApplicationProvider.getApplicationContext(), FILE_NAME);
        Optional<GeocodingCache> maybeCache = GeocodingCache.loadFromAndroidCache(ApplicationProvider.getApplicationContext(), FILE_NAME, base);

        assertThat(maybeCache.isPresent(), is(true));
        GeocodingCache cacheL = maybeCache.get();


        cacheL.getBestNamedCoordinates(new Coordinates(0, 0));
        cacheL.getNamedCoordinates("Somewhere", 1);
        cacheL.getBestNamedCoordinates(new Coordinates(0, 0));
        cacheL.getNamedCoordinates("Somewhere", 1);
        assertThat(base.count, is(2));


        cacheL.getNamedCoordinates("SomewhereElse", 1);
        assertThat(base.count, is(3));
    }


}
