package com.github.onedirection.geocoding;

import com.github.onedirection.utils.Pair;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

interface GeocodingService {

    default CompletableFuture<Coordinates> getBestCoordinates(String locationName){
        return getBestNamedCoordinates(locationName).thenApply(p -> p.first);
    }

    CompletableFuture<Pair<Coordinates, String>> getBestNamedCoordinates(String locationName);

}
