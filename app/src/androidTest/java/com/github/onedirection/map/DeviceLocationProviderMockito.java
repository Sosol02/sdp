package com.github.onedirection.map;

import android.location.Location;

import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.location.DeviceLocationProvider;
import com.github.onedirection.utils.ObserverPattern;
import com.google.android.gms.location.LocationCallback;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class DeviceLocationProviderMockito implements DeviceLocationProvider {

    private final ArrayList<ObserverPattern.Observer<Coordinates>> observers = new ArrayList<>();

    @Override
    public CompletableFuture<Boolean> requestFineLocationPermission() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Location getLastAndroidLocation() {
        Location location = new Location("test");
        location.setLatitude(MapFragmentTest.LOCATION_1_latitude);
        location.setLongitude(MapFragmentTest.LOCATION_1_longitude);
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
        return MapFragmentTest.COORDINATES_1;
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
