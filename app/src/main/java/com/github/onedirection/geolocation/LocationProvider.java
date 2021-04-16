package com.github.onedirection.geolocation;

import android.app.Activity;

import java.util.concurrent.CompletableFuture;

public interface LocationProvider {

    Coordinates getLastLocation();
    CompletableFuture<Boolean> startLocationTracking();
    CompletableFuture<Boolean> stopLocationTracking();
}
