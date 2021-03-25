package com.github.onedirection.geocoding;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import static com.github.onedirection.utils.ObserverPattern.Observable;
import static com.github.onedirection.utils.ObserverPattern.Observer;

public final class DeviceLocation implements Observable<Coordinates>, LocationProvider {

    public static CompletableFuture<Coordinates> getCurrentLocation(Activity callingActivity) {
        CompletableFuture<Coordinates> result = new CompletableFuture<>();

        DeviceLocation self = new DeviceLocation(callingActivity);
        self.addObserver(new Observer<Coordinates>() {
            @Override
            public void onObservableUpdate(Observable<Coordinates> source, Coordinates coords) {
                self.removeObserver(this);
                result.complete(coords);
            }
        });
        if(!self.startLocationTracking()){
            result.completeExceptionally(new RuntimeException("Could not start location tracking."));
        }
        return result;
    }

    private final Activity callingActivity;
    private final FusedLocationProviderClient fusedLocationClient;
    private boolean requestingLocationUpdates = false;
    private final ArrayList<ObserverPattern.Observer<Coordinates>> observers = new ArrayList<>();
    private Location lastLocation;


    public DeviceLocation(Activity callingActivity) {
        Objects.requireNonNull(callingActivity);
        this.callingActivity = callingActivity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(callingActivity);

        if (!fineLocationUsageIsAllowed()) {
            requestFineLocationPermission();
        }
    }


    public boolean startLocationTracking() {
        if (!fineLocationUsageIsAllowed()) {
            return false;
        }
        LocationRequest locationRequest = createLocationRequest();
        return true;
    }


    public boolean fineLocationUsageIsAllowed() {
        return ActivityCompat.checkSelfPermission(callingActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public Coordinates getLastLocation() {
        return new Coordinates(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public void requestFineLocationPermission() {
        if (!fineLocationUsageIsAllowed()) {
            ActivityCompat.requestPermissions(callingActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, R.integer.location_permission_code);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                           int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(permissions.length == 0){
//            //permission request cancelled
//            return;
//        }
//        if(requestCode == R.integer.location_permission_code && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//
//        }
//    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(callingActivity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(callingActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates(locationRequest);
            }
        });

        task.addOnFailureListener(callingActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(callingActivity,
                                R.integer.request_change_settings);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
        return locationRequest;
    }

    private LocationCallback createLocationCallback() {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                lastLocation = locationResult.getLastLocation();
                for (Location location : locationResult.getLocations()) {
                    notifyOfLocationChange();
                }
            }
        };
        return locationCallback;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates(LocationRequest locationRequest) {
        if (fineLocationUsageIsAllowed()) {
            requestingLocationUpdates = true;
            fusedLocationClient.requestLocationUpdates(locationRequest,
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
    public boolean addObserver(@NonNull Observer<Coordinates> dataBufferObserver) {
        observers.add(dataBufferObserver);
        return true;
    }

    @Override
    public boolean removeObserver(@NonNull Observer<Coordinates> dataBufferObserver) {
        observers.remove(dataBufferObserver);
        return true;
    }
}
