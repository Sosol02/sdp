package com.github.onedirection.geolocation;

import android.app.Activity;

import java.util.concurrent.CompletableFuture;

public interface LocationProvider {

    static CompletableFuture<Coordinates> getCurrentLocation(Activity callingActivity) {
        return DeviceLocationProvider.getCurrentLocation(callingActivity);
    }

    CompletableFuture<Boolean> startLocationTracking();
    boolean fineLocationUsageIsAllowed();
    Coordinates getLastLocation();
    void requestFineLocationPermission();

}
