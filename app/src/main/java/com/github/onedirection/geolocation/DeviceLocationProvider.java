package com.github.onedirection.geolocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.github.onedirection.R;
import com.github.onedirection.utils.ObserverPattern;
import com.google.android.gms.common.api.ResolvableApiException;
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
import static com.github.onedirection.utils.ObserverPattern.Observable;
import static com.github.onedirection.utils.ObserverPattern.Observer;

public abstract class DeviceLocationProvider extends Activity implements Observable<Coordinates>, LocationProvider {

//    public static CompletableFuture<Coordinates> getCurrentLocation(Activity callingActivity) {
//        CompletableFuture<Coordinates> result = new CompletableFuture<>();
//
//        DeviceLocationProvider self = new DeviceLocationProvider(callingActivity);
//        self.addObserver(new Observer<Coordinates>() {
//            @Override
//            public void onObservableUpdate(Observable<Coordinates> source, Coordinates coords) {
//                self.removeObserver(this);
//                result.complete(coords);
//            }
//        });
//        self.startLocationTracking().thenAccept(b -> {
//            if(!b){
//                result.completeExceptionally(new RuntimeException("Could not start location tracking."));
//            }
//        });
//        return result;
//    }

    private final static LocationRequest LOCATION_REQUEST = LocationRequest.create();
    static {
        LOCATION_REQUEST.setInterval(10000);
        LOCATION_REQUEST.setFastestInterval(5000);
        LOCATION_REQUEST.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private final FusedLocationProviderClient fusedLocationClient;
    private final ArrayList<ObserverPattern.Observer<Coordinates>> observers;
    private Location lastLocation;
    private LocationCallback locationCallback;
    private CompletableFuture<Boolean> permissionRequestResult;


    public DeviceLocationProvider() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        observers =  new ArrayList<>();
        lastLocation = null;
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                notifyOfLocationChange();
            }
            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if(fineLocationUsageIsAllowed()){
                    createLocationRequest();
                }
            }
        };
        permissionRequestResult = null;
    }


    /**
     * Called to start the tracking after making sure that permission is granted
     * Returns a completable future which resolves to true if the permissions to start location tracking were given, false otherwise
     */
    @Override
    @SuppressLint("MissingPermission")
    public final CompletableFuture startLocationTracking() {
        requestFineLocationPermission();
        return permissionRequestResult.thenApply(permission -> {
                if(permission){
                    fusedLocationClient.requestLocationUpdates(LOCATION_REQUEST,
                            locationCallback,
                            Looper.getMainLooper());
                    createLocationRequest();
                    return CompletableFuture.completedFuture(true);
                }
                else{
                    return CompletableFuture.completedFuture(false);
                }
        });
    }

    @Override
    public final Coordinates getLastLocation() {
        return new Coordinates(lastLocation.getLatitude(), lastLocation.getLongitude());
    }


//    public CompletableFuture<Boolean> startLocationTracking() {
//        return requestFineLocationPermission().whenCompleteAsync((aBoolean, throwable) -> createLocationRequest());
//    }

    private void requestFineLocationPermission() {
        if (!fineLocationUsageIsAllowed()) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, R.integer.location_permission_code);
        } else{
            permissionRequestResult = CompletableFuture.completedFuture(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionRequestResult = new CompletableFuture<Boolean>();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == R.integer.location_permission_code){
            for(int i = 0; i < permissions.length; ++i){
                if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    permissionRequestResult.complete(true);
                }
                else{
                    permissionRequestResult.complete(false);
                }
            }
        }
    }

    private boolean fineLocationUsageIsAllowed() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private CompletableFuture<Boolean> createLocationRequest() {
        Log.d("Testing", "Request created");
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(LOCATION_REQUEST);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(
                this,
                locationSettingsResponse -> {
                    result.complete(true);
                });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(this, R.integer.request_change_settings);
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
