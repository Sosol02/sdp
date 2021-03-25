package com.github.onedirection.geocoding;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.onedirection.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.data.DataBufferObserver;
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

public final class DeviceLocation extends AppCompatActivity implements DataBufferObserver.Observable {

    private final Activity callingActivity;
    private final FusedLocationProviderClient fusedLocationClient;
    private boolean requestingLocationUpdates = false;
    private ArrayList<DataBufferObserver> observers = new ArrayList<>();
    private Location lastLocation;


    public DeviceLocation(Activity callingActivity) {
        Objects.requireNonNull(callingActivity);
        this.callingActivity = callingActivity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(callingActivity);

        if(!fineLocationUsageIsAllowed()){
            requestFineLocationPermission();
        }
    }


    public boolean startLocationTracking(){
        if(!fineLocationUsageIsAllowed()){
            return false;
        }
        LocationRequest locationRequest = createLocationRequest();
        startLocationUpdates(locationRequest);
        return true;
    }


    private boolean fineLocationUsageIsAllowed(){
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public Location getLastLocation() {
        return lastLocation;
    }

    public void requestFineLocationPermission(){
        if (!fineLocationUsageIsAllowed()) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, R.integer.location_permission_code);
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
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates(locationRequest);
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
        return locationRequest;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
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

    private void notifyOfLocationChange(){
        for(DataBufferObserver observer : observers){
            observer.onDataChanged();
        }
    }

    @Override
    public void addObserver(@NonNull DataBufferObserver dataBufferObserver) {
        observers.add(dataBufferObserver);
    }

    @Override
    public void removeObserver(@NonNull DataBufferObserver dataBufferObserver) {
        observers.remove(dataBufferObserver);
    }
}
