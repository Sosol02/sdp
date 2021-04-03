package com.github.onedirection.geolocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;

import com.github.onedirection.R;
import com.github.onedirection.utils.ObserverPattern;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import static com.github.onedirection.utils.ObserverPattern.Observable;
import static com.github.onedirection.utils.ObserverPattern.Observer;

public final class DeviceLocationProvider implements Observable<Coordinates>, LocationProvider {

    public static CompletableFuture<Coordinates> getCurrentLocation(Activity callingActivity) {
        CompletableFuture<Coordinates> result = new CompletableFuture<>();

        DeviceLocationProvider self = new DeviceLocationProvider(callingActivity);
        self.addObserver(new Observer<Coordinates>() {
            @Override
            public void onObservableUpdate(Observable<Coordinates> source, Coordinates coords) {
                self.removeObserver(this);
                result.complete(coords);
            }
        });
        self.startLocationTracking().thenAccept(b -> {
            if(!b){
                result.completeExceptionally(new RuntimeException("Could not start location tracking."));
            }
        });
        return result;
    }

    private final static LocationRequest LOCATION_REQUEST = LocationRequest.create();
    static {
        LOCATION_REQUEST.setInterval(10000);
        LOCATION_REQUEST.setFastestInterval(5000);
        LOCATION_REQUEST.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private final Activity callingActivity;
    private final FusedLocationProviderClient fusedLocationClient;
    private final ArrayList<ObserverPattern.Observer<Coordinates>> observers = new ArrayList<>();
    private Location lastLocation;

    @VisibleForTesting
    public DeviceLocationProvider(Activity callingActivity, FusedLocationProviderClient provider){
        this.callingActivity = Objects.requireNonNull(callingActivity);
        this.fusedLocationClient = Objects.requireNonNull(provider);

        if (!fineLocationUsageIsAllowed()) {
            requestFineLocationPermission();
        }
    }

    public DeviceLocationProvider(Activity callingActivity) {
        this(callingActivity, LocationServices.getFusedLocationProviderClient(callingActivity));
    }

    @Override
    public CompletableFuture<Boolean> startLocationTracking() {
        if (!fineLocationUsageIsAllowed()) {
            return CompletableFuture.completedFuture(false);
        }
        return createLocationRequest();
    }


    @Override
    public boolean fineLocationUsageIsAllowed() {
        return ActivityCompat.checkSelfPermission(callingActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public Coordinates getLastLocation() {
        return new Coordinates(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    @Override
    public void requestFineLocationPermission() {
        if (!fineLocationUsageIsAllowed()) {
            ActivityCompat.requestPermissions(callingActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, R.integer.location_permission_code);
        }
    }

    private CompletableFuture<Boolean> createLocationRequest() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(LOCATION_REQUEST);
        SettingsClient client = LocationServices.getSettingsClient(callingActivity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(
                callingActivity,
                locationSettingsResponse -> {
                    startLocationUpdates();
                    result.complete(true);
                });

        task.addOnFailureListener(callingActivity, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(callingActivity, R.integer.request_change_settings);
                    // TODO: We should most likely do something more (onActivityResult ?)
                    createLocationRequest().whenComplete((aBoolean, throwable) -> {
                        if(aBoolean != null){
                            result.complete(aBoolean);
                        }
                        else{
                            result.completeExceptionally(throwable);
                        }
                    });
                } catch (IntentSender.SendIntentException sendEx) {
                    result.complete(false);
                }
            }
        });

        return result;
    }

    private LocationCallback createLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                lastLocation = locationResult.getLastLocation();
                notifyOfLocationChange();
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (fineLocationUsageIsAllowed()) {
            fusedLocationClient.requestLocationUpdates(LOCATION_REQUEST,
                    createLocationCallback(),
                    Looper.getMainLooper());
        }
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
