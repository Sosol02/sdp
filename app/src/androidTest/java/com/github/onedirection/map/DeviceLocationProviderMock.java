package com.github.onedirection.map;

import android.location.Location;

import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.geolocation.location.DeviceLocationProvider;
import com.github.onedirection.utils.ObserverPattern;
import com.google.android.gms.location.LocationCallback;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class DeviceLocationProviderMock implements DeviceLocationProvider {

    public static final double LOCATION_1_latitude = 32.22222;
    public static final double LOCATION_1_longitude = 43.33333;
    public static final Coordinates COORDINATES_LOCATION = new Coordinates(32.22222, 43.33333);

    private final ArrayList<ObserverPattern.Observer<Coordinates>> observers = new ArrayList<>();
    private final boolean locationPermission;

    public DeviceLocationProviderMock(boolean locationPermission) {
        this.locationPermission = locationPermission;
    }

    @Override
    public CompletableFuture<Boolean> requestFineLocationPermission() {
        return CompletableFuture.completedFuture(locationPermission);
    }

    @Override
    public Location getLastAndroidLocation() {
        if (!locationPermission) {
            return null;
        }
        Location location = new Location("test");
        location.setLatitude(LOCATION_1_latitude);
        location.setLongitude(LOCATION_1_longitude);
        return location;
    }

    @Override
    public void setLocationCallBackNavigation(LocationCallback locationCallBackNavigation) {

    }

    @Override
    public void clearLocationCallBackNavigation() {

    }

    @Override
    public Coordinates getLastLocation() {
        if (!locationPermission) {
            return null;
        }
        return COORDINATES_LOCATION;
    }

    @Override
    public CompletableFuture<Boolean> startLocationTracking() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> stopLocationTracking() {
        return CompletableFuture.completedFuture(true);
    }

    public void notifyObservers() {
        for (ObserverPattern.Observer<Coordinates> observer : observers) {
            observer.onObservableUpdate(this, getLastLocation());
        }
    }

    @Override
    public boolean addObserver(ObserverPattern.Observer<Coordinates> observer) {
        return observers.add(observer);
    }

    @Override
    public boolean removeObserver(ObserverPattern.Observer<Coordinates> observer) {
        return observers.remove(observer);
    }
}
