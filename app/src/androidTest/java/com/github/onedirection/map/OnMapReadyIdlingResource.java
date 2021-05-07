package com.github.onedirection.map;

import androidx.annotation.NonNull;
import androidx.test.espresso.IdlingResource;

import com.github.onedirection.navigation.fragment.map.MapFragment;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.lang.reflect.Field;

public class OnMapReadyIdlingResource implements IdlingResource, OnMapReadyCallback {

    private MapboxMap mapboxMap;
    private MapFragment fragment;
    private IdlingResource.ResourceCallback resourceCallback;

    public OnMapReadyIdlingResource(MapFragment fragment) {
        this.fragment = fragment;
        this.mapboxMap = null;
        this.resourceCallback = null;
        try {
            Field field = fragment.getClass().getDeclaredField("mapView");
            field.setAccessible(true);
            ((MapView) field.get(fragment)).getMapAsync(this);
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isIdleNow() {
        return mapboxMap != null;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }

    public MapboxMap getMapboxMap() {
        return mapboxMap;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        if (resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
    }
}
