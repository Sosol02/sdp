package com.github.onedirection.geolocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public interface DeviceLocationProvider extends LocationProvider {
    static boolean fineLocationUsageIsAllowed(Context ctx) {
        return ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
