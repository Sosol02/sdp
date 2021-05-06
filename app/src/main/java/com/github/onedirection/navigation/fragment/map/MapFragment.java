package com.github.onedirection.navigation.fragment.map;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.onedirection.R;
import com.github.onedirection.event.Event;
import com.github.onedirection.geolocation.location.AbstractDeviceLocationProvider;
import com.github.onedirection.geolocation.location.DeviceLocationProvider;
import com.github.onedirection.utils.EspressoIdlingResource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class MapFragment extends Fragment {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private MarkerSymbolManager markerSymbolManager;
    private MyLocationSymbolManager myLocationSymbolManager;
    private RoutesManager routesManager;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView event_name;
    private TextView event_time_start;
    private TextView event_time_end;
    private TextView event_location;
    private DeviceLocationProvider deviceLocationProvider;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private CompletableFuture<Boolean> permissionRequestResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token));

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        permissionRequestResult = CompletableFuture.completedFuture(false);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                result -> {
                    permissionRequestResult.complete(result);
                    if (result) {
                        deviceLocationProvider.startLocationTracking();
                        if (myLocationSymbolManager != null) {
                            myLocationSymbolManager.SetEnableSymbol(true);
                        }
                    } else {
                        if (myLocationSymbolManager != null) {
                            myLocationSymbolManager.SetEnableSymbol(false);
                        }
                    }
                });

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            this.mapboxMap = mapboxMap;

            EspressoIdlingResource.getInstance().lockIdlingResource();
            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                initializeManagers(style);
                EspressoIdlingResource.getInstance().unlockIdlingResource();
            });

            initializeDeviceLocationProvider();

            view.findViewById(R.id.my_location_button).setOnClickListener(view1 -> {
                OnMyLocationButtonClickResponse();
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
        ZonedDateTime start = event.getStartTime();
        event_time_start.setText(String.format(Locale.getDefault(),
                "%s %s %dh%d",
                start.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()),
                start.getDayOfMonth(), start.getHour(), start.getMinute()));
        ZonedDateTime end = event.getEndTime();
        event_time_end.setText(String.format(Locale.getDefault(),
                "%s %s %dh%d",
                end.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()),
                end.getDayOfMonth(), end.getHour(), end.getMinute()));
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

    private CompletableFuture<Boolean> requestLocationPermission() {
        if (!DeviceLocationProvider.fineLocationUsageIsAllowed(requireContext().getApplicationContext())) {
            permissionRequestResult = new CompletableFuture<>();
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            permissionRequestResult = CompletableFuture.completedFuture(true);
        }
        return permissionRequestResult;
    }

    private void initializeManagers(@NonNull Style style) {
        Context context = requireContext().getApplicationContext();
        routesManager = new RoutesManager(context, mapView, mapboxMap, style);
        markerSymbolManager = new MarkerSymbolManager(context, mapView, mapboxMap, style, this);
        myLocationSymbolManager = new MyLocationSymbolManager(context, mapView, mapboxMap, style);
    }

    private void initializeDeviceLocationProvider() {
        deviceLocationProvider = new AbstractDeviceLocationProvider(requireContext().getApplicationContext()) {
            @Override
            public CompletableFuture<Boolean> requestFineLocationPermission() {
                return requestLocationPermission();
            }
        };

        if (DeviceLocationProvider.fineLocationUsageIsAllowed(requireContext().getApplicationContext())) {
            deviceLocationProvider.startLocationTracking();
            if (myLocationSymbolManager != null) {
                myLocationSymbolManager.SetEnableSymbol(true);
            }
        }
        deviceLocationProvider.addObserver((subject, value) -> {
            if (myLocationSymbolManager != null) {
                myLocationSymbolManager.update(value);
            }
        });

    }

    private void OnMyLocationButtonClickResponse() {
        if (myLocationSymbolManager != null) {
            if (!DeviceLocationProvider.fineLocationUsageIsAllowed(requireContext().getApplicationContext())) {
                permissionRequestResult = new CompletableFuture<>();
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            LatLng latLng = myLocationSymbolManager.getPosition();
            if (latLng != null) {
                CameraPosition position = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15)
                        .build();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
            }
        }
    }

}