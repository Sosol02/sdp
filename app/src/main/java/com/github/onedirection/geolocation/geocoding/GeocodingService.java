package com.github.onedirection.geolocation.geocoding;

import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.utils.Monads;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

/**
 * General interface implemented by all geocoding services.
 * Geocoding consist of mapping a name into its geographical coordinates.
 */
public interface GeocodingService {

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
