package com.github.onedirection.navigation.fragment.map;

import android.util.Log;

import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.DeviceLocationProvider;

import java.util.concurrent.CompletableFuture;

import static com.github.onedirection.utils.ObserverPattern.Observable;
import static com.github.onedirection.utils.ObserverPattern.Observer;


public class DeviceLocationEngine extends DeviceLocationProvider implements Observer<Coordinates> {

    public DeviceLocationEngine() {
        super.initializeDeviceLocationProvider();
        super.startLocationTracking();
        super.addObserver(this);
    }



    @Override
    public void onObservableUpdate(Observable<Coordinates> subject, Coordinates value) {
        Log.i("hmm", "lat : " + value.latitude + "long:" + value.longitude);
    }
}
