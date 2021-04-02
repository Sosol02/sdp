package com.github.onedirection.navigation.fragment.map;


import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.onedirection.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private MarkerSymbolManager markerSymbolManager;
    public static final String SYMBOL_ID = "MARKER_MAP";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            this.mapboxMap = mapboxMap;

            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                //enableLocationComponent(style);
                initializeMarkerSymbolManager(style);
            });


            view.findViewById(R.id.my_location_button).setOnClickListener(view1 -> {
                //TODO add location service
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(51.50550, -0.07520))
                        .zoom(15)
                        .tilt(20)
                        .build();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
            });

            mapboxMap.addOnMapClickListener(point -> {
                markerSymbolManager.removeAllMarker();
                markerSymbolManager.addMarker(point);
                return false;
            });
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void initializeMarkerSymbolManager(@NonNull Style styleOnLoaded) {
        Drawable marker = ContextCompat.getDrawable(getContext(), R.drawable.ic_marker_map);
        styleOnLoaded.addImage(SYMBOL_ID, BitmapUtils.getBitmapFromDrawable(marker));
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, styleOnLoaded);
        this.markerSymbolManager = new MarkerSymbolManager(symbolManager);
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        LocationComponentOptions customLocationComponentOptions =
                LocationComponentOptions.builder(getContext())
                        .build();

        LocationComponent locationComponent = mapboxMap.getLocationComponent();

        locationComponent.activateLocationComponent(LocationComponentActivationOptions
                .builder(getContext(), loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build());

        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.NORMAL);
    }
}