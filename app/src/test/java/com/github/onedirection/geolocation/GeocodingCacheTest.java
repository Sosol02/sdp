package com.github.onedirection.geolocation;

import com.github.onedirection.geolocation.geocoding.GeocodingCache;
import com.github.onedirection.geolocation.geocoding.GeocodingService;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GeocodingCacheTest {

    // List.of is not found for some reasons...
    private static <T> List<T> mkList(T elem){
        List<T> ls = new ArrayList<>();
        ls.add(elem);
        return ls;
    }

    private final static class DummyGeocoding implements GeocodingService {

        private int count = 0;

        @Override
        public CompletableFuture<List<NamedCoordinates>> getNamedCoordinates(String locationName, int count) {
            this.count += 1;
            return CompletableFuture.completedFuture(mkList(
                    new NamedCoordinates(locationName.hashCode(), count, locationName)
            ));
        }

        @Override
        public CompletableFuture<NamedCoordinates> getBestNamedCoordinates(Coordinates coordinates) {
            this.count += 1;
            return CompletableFuture.completedFuture(
                    new NamedCoordinates(coordinates, coordinates.toString())
            );
        }

        public int getCount(){
            return this.count;
        }
    }

    @Test
    public void uselessRequestsAreNotSent() throws ExecutionException, InterruptedException {

        GeocodingService ref = new DummyGeocoding();
        DummyGeocoding backing = new DummyGeocoding();
        GeocodingCache cached = new GeocodingCache(backing);

        String input1 = "Potato chips";
        int count1 = 10;
        List<NamedCoordinates> result1 = cached.getNamedCoordinates(input1, count1).get();
        assertThat(result1, is(ref.getNamedCoordinates(input1, count1).get()));
        assertThat(backing.getCount(), is(1));

        String input3 = "French fries";
        int count3 = 3;
        List<NamedCoordinates> result3 = cached.getNamedCoordinates(input3, count3).get();
        assertThat(result3, is(ref.getNamedCoordinates(input3, count3).get()));
        assertThat(backing.getCount(), is(2));

        List<NamedCoordinates> result2 = cached.getNamedCoordinates(input1, count1).get();
        assertThat(result2, is(ref.getNamedCoordinates(input1, count1).get()));
        assertThat(backing.getCount(), is(2));

        Coordinates input4 = new Coordinates(13.1, 132312);
        NamedCoordinates result4 = cached.getBestNamedCoordinates(input4).get();
        assertThat(result4, is(ref.getBestNamedCoordinates(input4).get()));
        assertThat(backing.getCount(), is(3));
    }

}
