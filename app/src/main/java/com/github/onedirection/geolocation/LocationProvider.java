package com.github.onedirection.geolocation;

import java.util.concurrent.CompletableFuture;

public interface LocationProvider {

    CompletableFuture<Boolean> requestFineLocationPermission();
    Coordinates getLastLocation();
    CompletableFuture<Boolean> startLocationTracking();
    CompletableFuture<Boolean> stopLocationTracking();
}
