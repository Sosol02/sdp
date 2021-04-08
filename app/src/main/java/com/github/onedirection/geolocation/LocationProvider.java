package com.github.onedirection.geolocation;

import android.app.Activity;

import java.util.concurrent.CompletableFuture;

public interface LocationProvider {


    //CompletableFuture<Boolean> startLocationTracking();
    Coordinates getLastLocation();
    CompletableFuture startLocationTracking();
}
