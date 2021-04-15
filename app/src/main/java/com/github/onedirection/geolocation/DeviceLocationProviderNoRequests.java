package com.github.onedirection.geolocation;

import android.content.Context;

import java.util.concurrent.CompletableFuture;

/**
 * A device location provider which doesn't ask for tracking permission.
 */
public class DeviceLocationProviderNoRequests extends AbstractDeviceLocationProvider {
    public DeviceLocationProviderNoRequests(Context ctx) {
        super(ctx);
    }

    /**
     * @return True if the location tracking is already allowed, false otherwise.
     */
    @Override
    public CompletableFuture<Boolean> requestFineLocationPermission() {
        return CompletableFuture
                .completedFuture(DeviceLocationProvider.fineLocationUsageIsAllowed(getContext()));
    }
}
