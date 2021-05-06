package com.github.onedirection.navigation.fragment.map;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.onedirection.BuildConfig;
import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.location.AbstractDeviceLocationProvider;
import com.github.onedirection.geolocation.location.DeviceLocationProvider;
import com.github.onedirection.utils.ObserverPattern;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.internal.NavigationManagerImpl;
import com.mapquest.navigation.internal.dataclient.NavigationRouteService;
import com.mapquest.navigation.listener.DefaultNavigationProgressListener;
import com.mapquest.navigation.listener.RerouteBehaviorOverride;
import com.mapquest.navigation.listener.RerouteListener;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.UserLocationTrackingConsentStatus;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Location;
import com.mapquest.navigation.model.location.LocationObservation;

import java.util.Objects;

public class NavigationManager {

    private com.mapquest.navigation.NavigationManager navigationManager;
    private MapboxMap mapboxMap;
    private Location lastLocation;

    private CenteringMapOnLocationProgressListener centeringMapOnLocationProgressListener;
    private RouteUpdatingRerouteListener routeUpdatingRerouteListener;

    private static final double NAVIGATION_ZOOM = 16;
    private static final double NAVIGATION_TILT_VALUE_DEGREES = 60;

    public NavigationManager(Context context, DeviceLocationProvider deviceLocationProvider,
                             MapboxMap mapboxMap, RouteDisplayManager routeDisplayManager) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(deviceLocationProvider);
        this.mapboxMap = mapboxMap;
        deviceLocationProvider.addObserver((subject, value) -> {

        });
        navigationManager = new com.mapquest.navigation.NavigationManager.Builder(context,
                BuildConfig.API_KEY, new DeviceLocationProviderAdapter(deviceLocationProvider))
                .build();
        routeUpdatingRerouteListener = new RouteUpdatingRerouteListener(routeDisplayManager);
        centeringMapOnLocationProgressListener = new CenteringMapOnLocationProgressListener();
    }

    public void startNavigation(@NonNull Route route) {
        navigationManager.initialize();
        navigationManager.setUserLocationTrackingConsentStatus(UserLocationTrackingConsentStatus.DENIED);
        navigationManager.addProgressListener(centeringMapOnLocationProgressListener);
        navigationManager.setRerouteBehaviorOverride(new RerouteBehaviorOverride() {
            @Override
            public boolean shouldReroute(Coordinate coordinate) {
                return true;
            }
        });
        navigationManager.addRerouteListener(routeUpdatingRerouteListener);
        navigationManager.startNavigation(route);

        Location location = navigationManager.getLocationProviderAdapter().getLastKnownLocation();
        CameraPosition cameraPosition = new CameraPosition.Builder(mapboxMap.getCameraPosition())
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .bearing(location.getBearing())
                .zoom(NAVIGATION_ZOOM)
                .tilt(NAVIGATION_TILT_VALUE_DEGREES)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000);
    }

    public void stopNavigation() {
        navigationManager.cancelNavigation();
        navigationManager.deinitialize();
    }

    private class CenteringMapOnLocationProgressListener extends DefaultNavigationProgressListener {
        @Override
        public void onLocationObservationReceived(@NonNull LocationObservation locationObservation) {
            Location location = locationObservation.getRawGpsLocation();
            CameraPosition cameraPosition = new CameraPosition.Builder(mapboxMap.getCameraPosition())
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .bearing(location.getBearing())
                    .zoom(NAVIGATION_ZOOM)
                    .tilt(NAVIGATION_TILT_VALUE_DEGREES)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000);
        }
    }

    private class RouteUpdatingRerouteListener implements RerouteListener {

        private RouteDisplayManager routeDisplayManager;

        public RouteUpdatingRerouteListener(RouteDisplayManager routeDisplayManager) {
            this.routeDisplayManager = routeDisplayManager;
        }

        @Override
        public void onRerouteWouldOccur(Location location) {

        }

        @Override
        public void onRerouteRequested(Location location) {

        }

        @Override
        public void onRerouteReceived(Route route) {
            routeDisplayManager.displayRoute(route);
        }

        @Override
        public void onRerouteFailed() {

        }
    }

}
