package com.github.onedirection.geolocation.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.utils.Monads;
import com.github.onedirection.utils.ObserverPattern;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.github.onedirection.utils.ObserverPattern.Observer;

/**
 * A partial implementation of a device location provider.
 * Most of the logic can be done in any context (thus this class), except
 * for the permission request, for whose no general-case solution was found.
 */
public abstract class AbstractDeviceLocationProvider implements DeviceLocationProvider {
    private final static LocationRequest LOCATION_REQUEST = LocationRequest.create();

    static {
        LOCATION_REQUEST.setInterval(10000);
        LOCATION_REQUEST.setFastestInterval(5000);
        LOCATION_REQUEST.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final ArrayList<ObserverPattern.Observer<Coordinates>> observers;
    private final LocationCallback locationCallback;
    private LocationCallback locationCallBackNavigation;
    private Location lastLocation;

    public AbstractDeviceLocationProvider(Context ctx) {
        this.context = ctx;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx);
        this.observers = new ArrayList<>();
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lastLocation = locationResult.getLastLocation();
                notifyOfLocationChange();
                if (locationCallBackNavigation != null) {
                    locationCallBackNavigation.onLocationResult(locationResult);
                }
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (DeviceLocationProvider.fineLocationUsageIsAllowed(ctx)) {
                    createLocationRequest();
                }
            }
        };
        this.lastLocation = null;
    }

    protected Context getContext() {
        return context;
    }

    //************************ Setup/Internal methods *****************************************

    private CompletableFuture<Boolean> createLocationRequest() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(LOCATION_REQUEST);
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(
                locationSettingsResponse ->
                        result.complete(locationSettingsResponse.getLocationSettingsStates().isLocationUsable())
        );

        task.addOnFailureListener(e -> result.complete(false));

        return result;
    }

    //************************ Methods to be a LocationProvider *****************************************

    /**
     * Only abstract method; it is so because this is the only thing we can't handle
     * generally (we need to be an activity to handle this easily).
     *
     * @return Whether the request was granted.
     * @see DeviceLocationProviderActivity
     * @see DeviceLocationProviderNoRequests
     */
    public abstract CompletableFuture<Boolean> requestFineLocationPermission();

    /**
     * Called to start the tracking after making sure that permission is granted
     * Returns a completable future which resolves to true if the permissions to start location tracking were given, false otherwise
     */
    @Override
    @SuppressLint("MissingPermission")
    public final CompletableFuture<Boolean> startLocationTracking() {
        return requestFineLocationPermission().whenComplete((permission, throwable) -> {
            if (permission) {
                fusedLocationClient.requestLocationUpdates(LOCATION_REQUEST,
                        locationCallback,
                        Looper.getMainLooper());
                createLocationRequest();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> stopLocationTracking() {
        return Monads.toFuture(fusedLocationClient.removeLocationUpdates(locationCallback))
                .thenApply(aVoid -> true);
    }

    @Override
    public final Coordinates getLastLocation() {
        if (lastLocation == null) {
            throw new IllegalStateException("There is no last location currently");
        }
        return new Coordinates(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public Location getLastAndroidLocation() {
        if (lastLocation == null) {
            throw new IllegalStateException("There is no last location currently");
        }
        return lastLocation;
    }

    public void setLocationCallBackNavigation(LocationCallback locationCallBackNavigation) {
        this.locationCallBackNavigation = locationCallBackNavigation;
    }

    public void clearLocationCallBackNavigation() {
        locationCallBackNavigation = null;
    }

    //************************ Methods to be Observable *****************************************

    private void notifyOfLocationChange() {
        for (Observer<Coordinates> observer : observers) {
            observer.onObservableUpdate(this, getLastLocation());
        }
    }

    @Override
    public boolean addObserver(Observer<Coordinates> observer) {
        Objects.requireNonNull(observer);
        observers.add(observer);
        return true;
    }

    @Override
    public boolean removeObserver(Observer<Coordinates> observer) {
        Objects.requireNonNull(observer);
        observers.remove(observer);
        return true;
    }
}
