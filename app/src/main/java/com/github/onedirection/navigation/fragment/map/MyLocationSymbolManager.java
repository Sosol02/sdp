package com.github.onedirection.navigation.fragment.map;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.github.onedirection.R;
import com.github.onedirection.geolocation.model.Coordinates;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.Objects;

/**
 * MyLocationSymbolManager is used with mapbox to be able to display the location user with a blue
 * dot on the map in mapfragment
 */
public class MyLocationSymbolManager {

    private final SymbolManager symbolManager;
    private Symbol myLocation;

    private final String MY_LOCATION_ID = "MY_LOCATION_MAP";
    private final float ICON_SIZE = 0.07f;

    public MyLocationSymbolManager(Context context, MapView mapView, MapboxMap mapboxMap, Style style) {
        Objects.requireNonNull(context);
        initializeSymbolImage(context, style);
        symbolManager = new SymbolManager(mapView, mapboxMap, style);
        symbolManager.setIconAllowOverlap(true);
    }

    public void updateCoordinates(Coordinates value) {
        if (value == null) {
            setEnableSymbol(false);
        } else {
            LatLng latLng = new LatLng(value.latitude, value.longitude);
            if (myLocation == null) {
                myLocation = symbolManager.create(new SymbolOptions()
                        .withLatLng(latLng)
                        .withIconImage(MY_LOCATION_ID)
                        .withIconSize(ICON_SIZE)
                );
            } else {
                myLocation.setLatLng(latLng);
                symbolManager.update(myLocation);
            }
            setEnableSymbol(true);
        }
    }

    public void setEnableSymbol(boolean enable) {
        if (myLocation != null) {
            myLocation.setIconOpacity(enable ? 1f : 0f);
        }
    }

    public LatLng getPosition() {
        return myLocation == null ? null : myLocation.getLatLng();
    }

    private void initializeSymbolImage(Context context, Style style) {
        Drawable myLocationSymbol = ContextCompat.getDrawable(context, R.mipmap.my_location_on_map);
        style.addImage(MY_LOCATION_ID, Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(myLocationSymbol)));
    }
}
