package com.github.onedirection.geolocation.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.onedirection.R;
import com.github.onedirection.geolocation.Coordinates;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import java.util.concurrent.CompletableFuture;

import static com.github.onedirection.utils.ObserverPattern.Observer;

/**
 * Device location provider which uses the activity extending it to
 * ask for user's permission to track its location.
 */
public abstract class DeviceLocationProviderActivity extends AppCompatActivity implements DeviceLocationProvider {
    private final static LocationRequest LOCATION_REQUEST = LocationRequest.create();

    static {
        LOCATION_REQUEST.setInterval(10000);
        LOCATION_REQUEST.setFastestInterval(5000);
        LOCATION_REQUEST.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private AbstractDeviceLocationProvider provider;
    private CompletableFuture<Boolean> permissionRequestResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DeviceLocationProviderActivity self = this;
        this.provider = new AbstractDeviceLocationProvider(this) {
            @Override
            public CompletableFuture<Boolean> requestFineLocationPermission() {
                return self.requestFineLocationPermission();
            }
        };
        this.permissionRequestResult = CompletableFuture.completedFuture(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == R.integer.location_permission_code) {
            for (int i = 0; i < permissions.length; ++i) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissionRequestResult.complete(true);
                }
            }
        }

        // Will be set only if not obtruded earlier
        permissionRequestResult.complete(false);
    }

    //************************ Methods to be a LocationProvider *****************************************

    public CompletableFuture<Boolean> requestFineLocationPermission() {
        if (!DeviceLocationProvider.fineLocationUsageIsAllowed(this)) {
            permissionRequestResult = new CompletableFuture<>();
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, R.integer.location_permission_code);
        } else {
            permissionRequestResult = CompletableFuture.completedFuture(true);
        }
        return permissionRequestResult;
    }

    /**
     * Called to start the tracking after making sure that permission is granted
     * Returns a completable future which resolves to true if the permissions to start location tracking were given, false otherwise
     */
    @Override
    @SuppressLint("MissingPermission")
    public final CompletableFuture<Boolean> startLocationTracking() {
        if (!permissionRequestResult.isDone())
            throw new IllegalStateException("Location tracking is already starting.");

        return provider.startLocationTracking();
    }

    @Override
    public CompletableFuture<Boolean> stopLocationTracking() {
        return provider.stopLocationTracking();
    }

    @Override
    public final Coordinates getLastLocation() {
        return provider.getLastLocation();
    }

    //************************ Methods to be Observable *****************************************

    @Override
    public boolean addObserver(Observer<Coordinates> observer) {
        return provider.addObserver(observer);
    }

    @Override
    public boolean removeObserver(Observer<Coordinates> observer) {
        return provider.removeObserver(observer);
    }


    public Location getLastAndroidLocation() {
        return null;
    }

    public void setLocationCallBackNavigation(LocationCallback locationCallBackNavigation) {}

    public void clearLocationCallBackNavigation() {}
}
