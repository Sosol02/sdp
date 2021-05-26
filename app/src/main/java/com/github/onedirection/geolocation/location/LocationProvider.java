package com.github.onedirection.geolocation.location;

import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.utils.ObserverPattern;

import java.util.concurrent.CompletableFuture;

/**
 * Allow to follow the current location of the user.
 */
public interface LocationProvider extends ObserverPattern.Observable<Coordinates> {

    Coordinates getLastLocation();

    CompletableFuture<Boolean> startLocationTracking();

    CompletableFuture<Boolean> stopLocationTracking();

    boolean addObserver(ObserverPattern.Observer<Coordinates> observer);

    boolean removeObserver(ObserverPattern.Observer<Coordinates> observer);

}
