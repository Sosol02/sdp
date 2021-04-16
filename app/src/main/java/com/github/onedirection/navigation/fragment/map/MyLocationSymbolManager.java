package com.github.onedirection.navigation.fragment.map;

import android.util.Log;

import com.github.onedirection.geolocation.Coordinates;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

public class MyLocationSymbolManager {

    private final SymbolManager symbolManager;
    private Symbol myLocation;

    public MyLocationSymbolManager(SymbolManager symbolManager) {
        this.symbolManager = symbolManager;
        symbolManager.setIconAllowOverlap(true);
    }

    public void update(Coordinates value) {
        LatLng latLng = new LatLng(value.latitude, value.longitude);
        if (myLocation == null) {
            myLocation = symbolManager.create(new SymbolOptions()
                    .withLatLng(latLng)
                    .withIconImage(MapFragment.MY_LOCATION_ID)
                    .withIconSize(0.07f)
            );
        } else {
            myLocation.setLatLng(latLng);
            symbolManager.update(myLocation);
        }
    }

    public LatLng getPosition() {
        return myLocation == null ? null : myLocation.getLatLng();
    }
}
