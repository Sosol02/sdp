package com.github.onedirection.navigation.fragment.map;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.github.onedirection.R;
import com.github.onedirection.event.model.Event;
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
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.model.Route;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Fragment where the mapbox is displayed, it used to be able to locate events on a map and
 * navigate between them
 */
public class MapFragment extends Fragment {

    private static final String LOG_TAG = "MapFragment";

    private MapView mapView;
    private MapboxMap mapboxMap;

    private MarkerSymbolManager markerSymbolManager;
    private MyLocationSymbolManager myLocationSymbolManager;
    private RoutesManager routesManager;
    private RouteDisplayManager routeDisplayManager;
    private NavigationManager navigationManager;

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView event_name;
    private TextView event_time_start;
    private TextView event_time_end;
    private TextView event_location;
    private Event currentEvent;

    private DeviceLocationProvider deviceLocationProvider;
    private boolean isFirstUpdate;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private CompletableFuture<Boolean> permissionRequestResult;

    private Button navigationButton;
    private Button navigationRouteButton;
    private Button cancelButton;
    private Optional<Event> navigationStart = Optional.empty();
    private Optional<Event> navigationEnd = Optional.empty();

    @VisibleForTesting
    public final CountingIdlingResource waitForRoute = new CountingIdlingResource("waitForRoute");

    @VisibleForTesting
    public final CountingIdlingResource waitForNavStart = new CountingIdlingResource("waitForEventSet");

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
                            myLocationSymbolManager.setEnableSymbol(true);
                        }
                    } else {
                        if (myLocationSymbolManager != null) {
                            myLocationSymbolManager.setEnableSymbol(false);
                        }
                    }
                });
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            this.mapboxMap = mapboxMap;

            EspressoIdlingResource.getInstance().lockIdlingResource();
            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                initializeDeviceLocationProvider();
                initializeManagers(style, view);
                EspressoIdlingResource.getInstance().unlockIdlingResource();
            });

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

        navigationButton = view.findViewById(R.id.fragment_map_event_nav_button);
        navigationRouteButton = view.findViewById(R.id.fragment_map_event_nav_route_button);
        cancelButton = view.findViewById(R.id.fragment_map_event_nav_cancel);
        cancelNavigation();

        navigationButton.setOnClickListener(but -> {
            Log.d(LOG_TAG, "Navigation button pressed.");
            cancelNavigation();
            LatLng pos = myLocationSymbolManager.getPosition();
            if (pos == null) {
                Log.d(LOG_TAG, "Position is null...");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }

            waitForNavStart.increment();
            routesManager.findRoute(
                    Objects.requireNonNull(pos),
                    Collections.singletonList(Objects.requireNonNull(currentEvent.getCoordinates().get().toLatLng())),
                    new NavigationRouteResponseListener()
            );
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        navigationRouteButton.setOnClickListener(but -> {
            Log.d(LOG_TAG, "Nav button clicked: navigationStart: " + navigationStart + ", navigationEnd: " + navigationEnd);
            if (navigationStart.isPresent()) {
                Log.d(LOG_TAG, "Nav starting!");
                navigationEnd = Optional.of(currentEvent);
                waitForRoute.increment();
                routesManager.findRoute(
                        navigationStart.get().getCoordinates().get().toLatLng(),
                        Collections.singletonList(navigationEnd.get().getCoordinates().get().toLatLng()),
                        new DisplayRouteResponseListener()
                );
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                cancelNavigation();
            } else {
                navigationStart = Optional.of(currentEvent);
                cancelButton.setVisibility(View.VISIBLE);
                navigationRouteButton.setText(R.string.set_end_point);
                markerSymbolManager.setTripStartMarker(currentEvent.getCoordinates().get().toLatLng());
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        cancelButton.setOnClickListener(but -> cancelNavigation());
        isFirstUpdate = true;

        return view;
    }

    private void cancelNavigation() {
        Log.d(LOG_TAG, "Cancel navigation.");
        navigationStart = Optional.empty();
        navigationEnd = Optional.empty();
        cancelButton.setVisibility(View.GONE);
        navigationRouteButton.setText(R.string.set_starting_point);
        if (markerSymbolManager != null) {
            markerSymbolManager.removeTripStartMarker();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (markerSymbolManager != null)
            markerSymbolManager.syncEventsWithDb();
    }

    public void showBottomSheet() {
        Log.d(LOG_TAG, "showBottomSheet");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    
    public void setBottomSheetEvent(Event event) {
        Objects.requireNonNull(event);
        currentEvent = event;
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
    public void onStart() {
        super.onStart();
        if (markerSymbolManager != null)
            markerSymbolManager.syncEventsWithDb();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (markerSymbolManager != null)
            markerSymbolManager.syncEventsWithDb();
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
        if (isVisible() && !DeviceLocationProvider.fineLocationUsageIsAllowed(requireContext().getApplicationContext())) {
            permissionRequestResult = new CompletableFuture<>();
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            permissionRequestResult = CompletableFuture.completedFuture(true);
        }
        return permissionRequestResult;
    }

    private void initializeManagers(@NonNull Style style, @NonNull View view) {
        Context context = requireContext().getApplicationContext();
        markerSymbolManager = new MarkerSymbolManager(context, mapView, mapboxMap, style, this);
        myLocationSymbolManager = new MyLocationSymbolManager(context, mapView, mapboxMap, style);
        routesManager = new RoutesManager(context);
        routeDisplayManager = new RouteDisplayManager(mapView, mapboxMap, style);
        navigationManager = new NavigationManager(context, deviceLocationProvider, mapboxMap, routeDisplayManager, view);

        // Now that markerSymbolManager is non null, sync
        markerSymbolManager.syncEventsWithDb();
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
                myLocationSymbolManager.setEnableSymbol(true);
            }
        }
        deviceLocationProvider.addObserver((subject, value) -> {
            if (myLocationSymbolManager != null) {
                myLocationSymbolManager.update(value);
                if (isFirstUpdate) {
                    isFirstUpdate = false;
                    OnMyLocationButtonClickResponse();
                }
            }
        });
    }

    private void OnMyLocationButtonClickResponse() {
        if (myLocationSymbolManager != null) {
            EspressoIdlingResource.getInstance().lockIdlingResource();
            if (!DeviceLocationProvider.fineLocationUsageIsAllowed(requireContext().getApplicationContext())) {
                permissionRequestResult = new CompletableFuture<>();
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            deviceLocationProvider.startLocationTracking();
            LatLng latLng = myLocationSymbolManager.getPosition();
            if (latLng != null) {
                CameraPosition position = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15)
                        .build();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
            }
            EspressoIdlingResource.getInstance().unlockIdlingResource();
        }
    }

    private class DisplayRouteResponseListener implements RoutesResponseListener {

        @Override
        public void onRoutesRetrieved(@NonNull List<Route> list) {
            waitForRoute.decrement();
            if (list.size() > 0) {
                routeDisplayManager.displayRoute(list.get(0));
            }
        }

        @Override
        public void onRequestFailed(@Nullable Integer integer, @Nullable IOException e) {
            waitForRoute.decrement();
        }

        @Override
        public void onRequestMade() {}
    }

    private class NavigationRouteResponseListener implements RoutesResponseListener {

        @Override
        public void onRoutesRetrieved(@NonNull List<Route> list) {
            if (list.size() > 0) {
                if (waitForNavStart.isIdleNow()) {
                    waitForNavStart.decrement();
                }
                navigationManager.startNavigation(list.get(0));
            }
        }

        @Override
        public void onRequestFailed(@Nullable Integer integer, @Nullable IOException e) {
            //Intentionally left empty
        }

        @Override
        public void onRequestMade() {
            //Intentionally left empty
        }
    }
}