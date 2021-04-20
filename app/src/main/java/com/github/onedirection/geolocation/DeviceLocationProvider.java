package com.github.onedirection.geolocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.onedirection.R;
import com.github.onedirection.utils.Monads;
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

public abstract class DeviceLocationProvider extends AppCompatActivity implements Observable<Coordinates>, LocationProvider {
    private final static LocationRequest LOCATION_REQUEST = LocationRequest.create();
    static {
        LOCATION_REQUEST.setInterval(10000);
        LOCATION_REQUEST.setFastestInterval(5000);
        LOCATION_REQUEST.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private FusedLocationProviderClient fusedLocationClient;
    private ArrayList<ObserverPattern.Observer<Coordinates>> observers;
    private Location lastLocation;
    private LocationCallback locationCallback;
    private CompletableFuture<Boolean> permissionRequestResult;

    //************************ Setup/Internal methods *****************************************

    private void requestFineLocationPermission() {
        if (!fineLocationUsageIsAllowed()) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, R.integer.location_permission_code);
        } else{
            permissionRequestResult.complete(true);
        }
    }

    private CompletableFuture<Boolean> createLocationRequest() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(LOCATION_REQUEST);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(
                this,
                locationSettingsResponse ->
                        result.complete(locationSettingsResponse.getLocationSettingsStates().isLocationUsable())
        );

        task.addOnFailureListener(this, e -> result.complete(false));

        return result;
    }

    private boolean fineLocationUsageIsAllowed() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        observers =  new ArrayList<>();
        lastLocation = null;
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lastLocation = locationResult.getLastLocation();
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
        permissionRequestResult = CompletableFuture.completedFuture(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == R.integer.location_permission_code){
            for(int i = 0; i < permissions.length; ++i){
                if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    permissionRequestResult.complete(true);
                }
            }
        }

        // Will be set only if not obtruded earlier
        permissionRequestResult.complete(false);
    }

    //************************ Methods to be a LocationProvider *****************************************

    /**
     * Called to start the tracking after making sure that permission is granted
     * Returns a completable future which resolves to true if the permissions to start location tracking were given, false otherwise
     */
    @Override
    @SuppressLint("MissingPermission")
    public final CompletableFuture<Boolean> startLocationTracking() {
        if(!permissionRequestResult.isDone())
            throw new IllegalStateException("Location tracking is already starting.");

        permissionRequestResult = new CompletableFuture<>();
        requestFineLocationPermission();
        return permissionRequestResult.whenComplete((permission, throwable) -> {
                if(permission){
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
        if(lastLocation == null){
            throw new IllegalStateException("There is no last location currently");
        }
        return new Coordinates(lastLocation.getLatitude(), lastLocation.getLongitude());
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
