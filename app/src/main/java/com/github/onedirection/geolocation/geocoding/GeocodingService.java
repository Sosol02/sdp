package com.github.onedirection.geolocation.geocoding;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.geolocation.model.NamedCoordinates;
import com.github.onedirection.utils.Monads;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * General interface implemented by all geocoding services.
 * Geocoding consist of mapping a name into its geographical coordinates.
 */
public interface GeocodingService {

    static GeocodingService getDefaultInstance() {
        return GeocodingServices.getDefaultInstance();
    }

    static void init(Context ctx){
        GeocodingServices.init(ctx);
    }

    /**
     * @param locationName Some location's name.
     * @return The coordinates if the location is found.
     */
    default CompletableFuture<Coordinates> getBestCoordinates(String locationName){
        return getBestNamedCoordinates(locationName).thenApply(NamedCoordinates::dropName);
    }

    /**
     * @param locationName Some location's name.
     * @return A list of its possible coordinates.
     */
    default CompletableFuture<List<Coordinates>> getCoordinates(String locationName, int count){
        return getNamedCoordinates(locationName, count).thenApply(ls -> Monads.map(ls, NamedCoordinates::dropName));
    }

    /**
     * @param locationName Some location's name.
     * @return The coordinates and their associated name if the location is found.
     */
    default CompletableFuture<NamedCoordinates> getBestNamedCoordinates(String locationName) {
        return getNamedCoordinates(locationName, 1).thenCompose(r -> {
                CompletableFuture<NamedCoordinates> future = new CompletableFuture<>();
                if(r.isEmpty()){
                    future.completeExceptionally(new NoSuchElementException());
                }
                else{
                    future.complete(r.get(0));
                }
                return future;
            });
    }

    /**
     * @param locationName Some location's name.
     * @return A list of tis possible coordinates and their associated name.
     */
    CompletableFuture<List<NamedCoordinates>> getNamedCoordinates(String locationName, int count);

    /**
     * @param coordinates Some location on earth.
     * @return The coordinates and their associated name if the location is found.
     */
    CompletableFuture<NamedCoordinates> getBestNamedCoordinates(Coordinates coordinates);

}

/**
 * Static context for the geocoding interface.
 *
 * Contains default instances as well as logic to cache results.
 */
class GeocodingServices {
    private static final String LOGCAT_TAG = "GeocodingServices";

    private GeocodingServices() {}

    private final static String CACHE_FILE_NAME = "GeocodingCache";
    private final static int CACHING_DELAY_MS = 60 * 1000;

    private static GeocodingService DEFAULT = null;
    private static GeocodingCache CACHED_DEFAULT = null;

    static GeocodingService getDefaultInstance() {
        if(DEFAULT == null){
            throw new IllegalStateException("Geocoding not initialized");
        }

        return CACHED_DEFAULT;
    }

    static void init(Context ctx){
        DEFAULT = new NominatimGeocoding(ctx);
        Optional<GeocodingCache> maybeCache = GeocodingCache.loadFromAndroidCache(
                ctx,
                CACHE_FILE_NAME,
                DEFAULT
        );

        if(maybeCache.isPresent()){
            Log.d(LOGCAT_TAG, "Cache loaded from file");
            CACHED_DEFAULT = maybeCache.get();
        }
        else{
            Log.d(LOGCAT_TAG, "Cache could not be loaded");
            CACHED_DEFAULT = new GeocodingCache(DEFAULT);
        }

        Handler periodical = new Handler(ctx.getMainLooper());
        periodical.post(new Runnable() {
            @Override
            public void run() {
                Log.d(LOGCAT_TAG, "Dumping cache to file: " + CACHED_DEFAULT.dumpToAndroidCache(ctx, CACHE_FILE_NAME));
                periodical.postDelayed(this, CACHING_DELAY_MS);
            }
        });
    }

}