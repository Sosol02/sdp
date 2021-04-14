package com.github.onedirection.navigation.fragment.map;


import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.onedirection.R;
import com.github.onedirection.events.Event;
import com.github.onedirection.geolocation.DeviceLocationProvider;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;


public class MapFragment extends Fragment {

    private MapView mapView;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView event_name;
    private TextView event_time_start;
    private TextView event_time_end;
    private TextView event_location;
    private MapboxMap mapboxMap;
    private MarkerSymbolManager markerSymbolManager;
    private DeviceLocationProvider deviceLocationProvider;
    private MyLocationSymbolManager myLocationSymbolManager;
    private Symbol clickSymbol;

    public static final String SYMBOL_ID = "MARKER_MAP";
    public static final String MY_LOCATION_ID = "MY_LOCATION_MAP";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        deviceLocationProvider = (DeviceLocationProvider) requireActivity();
        deviceLocationProvider.startLocationTracking();
        deviceLocationProvider.addObserver((subject, value) -> {
            if (myLocationSymbolManager != null) {
                myLocationSymbolManager.update(value);
            }
        });

        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            this.mapboxMap = mapboxMap;

            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                initializeMarkerSymbolManager(style);
                initializeMyLocationSymbolManager(style);
            });

            view.findViewById(R.id.my_location_button).setOnClickListener(view1 -> {
                if (myLocationSymbolManager != null) {
                    LatLng latLng = myLocationSymbolManager.getPosition();
                    if (latLng != null) {
                        CameraPosition position = new CameraPosition.Builder()
                                .target(latLng)
                                .zoom(15)
                                .build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
                    }
                }
            });

            mapboxMap.addOnMapClickListener(point -> {
                if (clickSymbol != null)
                    markerSymbolManager.removeMarker(clickSymbol);
                clickSymbol = markerSymbolManager.addMarker(point);
                return false;
            });
        });

        View bottomSheet = view.findViewById(R.id.fragment_map_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        event_name = view.findViewById(R.id.fragment_map_event_name);
        event_time_start = view.findViewById(R.id.fragment_map_event_time_start);
        event_time_end = view.findViewById(R.id.fragment_map_event_time_end);
        event_location = view.findViewById(R.id.fragment_map_event_location);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void setBottomSheetEvent(Event event) {
        Objects.requireNonNull(event);
        event_name.setText(event.getName());
        ZonedDateTime start=event.getStartTime();
        event_time_start.setText(String.format("%s %s %dh%d",start.getMonth().getDisplayName(TextStyle.FULL_STANDALONE,Locale.getDefault()),start.getDayOfMonth(),start.getHour(),start.getMinute()));
        ZonedDateTime end=event.getEndTime();
        event_time_end.setText(String.format("%s %s %dh%d",end.getMonth().getDisplayName(TextStyle.FULL_STANDALONE,Locale.getDefault()),end.getDayOfMonth(),end.getHour(),end.getMinute()));
        event_location.setText(event.getLocationName());
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void initializeMarkerSymbolManager(@NonNull Style styleOnLoaded) {
        Drawable marker = ContextCompat.getDrawable(getContext(), R.drawable.ic_marker_map);
        styleOnLoaded.addImage(SYMBOL_ID, BitmapUtils.getBitmapFromDrawable(marker));
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, styleOnLoaded);
        this.markerSymbolManager = new MarkerSymbolManager(symbolManager, this);
    }

    private void initializeMyLocationSymbolManager(@NonNull Style styleOnLoaded) {
        Drawable myLocationSymbol = ContextCompat.getDrawable(getContext(), R.drawable.my_location_on_map);
        styleOnLoaded.addImage(MY_LOCATION_ID, BitmapUtils.getBitmapFromDrawable(myLocationSymbol));
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, styleOnLoaded);
        this.myLocationSymbolManager = new MyLocationSymbolManager(symbolManager);
    }
}