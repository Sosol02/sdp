package com.github.onedirection.geolocation;

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
