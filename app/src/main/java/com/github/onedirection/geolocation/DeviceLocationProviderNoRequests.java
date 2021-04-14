package com.github.onedirection.geolocation;

import android.content.Context;

import java.util.concurrent.CompletableFuture;

public class DeviceLocationProviderNoRequests extends AbstractDeviceLocationProvider {
    public DeviceLocationProviderNoRequests(Context ctx) {
        super(ctx);
    }

    @Override
    public CompletableFuture<Boolean> requestFineLocationPermission() {
        return CompletableFuture
                .completedFuture(DeviceLocationProvider.fineLocationUsageIsAllowed(getContext()));
    }
}
