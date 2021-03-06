package com.github.onedirection.geolocation.location;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;

import java.util.concurrent.CompletableFuture;


/**
 * Location provider relying on Android's location capabilities, and
 * as such requiring permissions from the system/user.
 */
public interface DeviceLocationProvider extends LocationProvider {
    static boolean fineLocationUsageIsAllowed(Context ctx) {
        return ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    CompletableFuture<Boolean> requestFineLocationPermission();

    Location getLastAndroidLocation();

    void setLocationCallBackNavigation(LocationCallback locationCallBackNavigation);

    void clearLocationCallBackNavigation();
}
