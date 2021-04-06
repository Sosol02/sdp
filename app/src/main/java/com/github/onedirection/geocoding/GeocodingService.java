package com.github.onedirection.geocoding;

import com.github.onedirection.utils.Monads;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * General interface implemented by all geocoding services.
 * Geocoding consist of mapping a name into its geographical coordinates.
 */
interface GeocodingService {

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
    CompletableFuture<NamedCoordinates> getBestNamedCoordinates(String locationName);

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
