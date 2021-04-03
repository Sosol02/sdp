package com.github.onedirection.geolocation;

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
        return getBestNamedCoordinates(locationName).thenApply(p -> p.dropName());
    }

    /**
     * @param locationName Some location's name.
     * @return The coordinates and their associated name if the location is found.
     */
    CompletableFuture<NamedCoordinates> getBestNamedCoordinates(String locationName);

    /**
     * @param coordinates Some location on earth.
     * @return The coordinates and their associated name if the location is found.
     */
    CompletableFuture<NamedCoordinates> getBestNamedCoordinates(Coordinates coordinates);

}
