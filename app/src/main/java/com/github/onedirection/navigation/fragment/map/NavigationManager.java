package com.github.onedirection.navigation.fragment.map;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.onedirection.BuildConfig;
import com.github.onedirection.geolocation.location.DeviceLocationProvider;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapquest.navigation.listener.DefaultNavigationProgressListener;
import com.mapquest.navigation.listener.NavigationStateListener;
import com.mapquest.navigation.listener.RerouteBehaviorOverride;
import com.mapquest.navigation.listener.RerouteListener;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteStoppedReason;
import com.mapquest.navigation.model.UserLocationTrackingConsentStatus;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Location;
import com.mapquest.navigation.model.location.LocationObservation;

import java.util.Objects;

/**
 * NavigationManager is used with mapquest to be able to navigate between events on the map
 * in mapfragment by giving it a route
 */
public class NavigationManager {

    private final com.mapquest.navigation.NavigationManager navigationManager;
    private final MapboxMap mapboxMap;
    private final Context context;
    private Location lastLocation;

    private final CenteringMapOnLocationProgressListener centeringMapOnLocationProgressListener;
    private final RouteUpdatingRerouteListener routeUpdatingRerouteListener;

    private static final double NAVIGATION_ZOOM = 16;
    private static final double NAVIGATION_TILT_VALUE_DEGREES = 60;

    public NavigationManager(Context context, DeviceLocationProvider deviceLocationProvider,
                             MapboxMap mapboxMap, RouteDisplayManager routeDisplayManager) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(deviceLocationProvider);
        this.mapboxMap = mapboxMap;
        this.context = context;
        navigationManager = new com.mapquest.navigation.NavigationManager.Builder(context,
                BuildConfig.API_KEY, new DeviceLocationProviderAdapter(deviceLocationProvider))
                .build();
        navigationManager.setRerouteBehaviorOverride(coordinate -> true);
        routeUpdatingRerouteListener = new RouteUpdatingRerouteListener(routeDisplayManager);
        centeringMapOnLocationProgressListener = new CenteringMapOnLocationProgressListener();
    }

    public void startNavigation(@NonNull Route route) {
        navigationManager.initialize();
        navigationManager.setUserLocationTrackingConsentStatus(UserLocationTrackingConsentStatus.DENIED);
        navigationManager.addProgressListener(centeringMapOnLocationProgressListener);
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
        navigationManager.removeProgressListener(centeringMapOnLocationProgressListener);
        navigationManager.removeRerouteListener(routeUpdatingRerouteListener);
        navigationManager.deinitialize();
    }

    private class ToastUpdateNavigationStateListener extends NavigationStateListener {

        @Override
        public void onNavigationStarted() {
            Toast.makeText(context, "Navigation started", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNavigationStopped(@NonNull RouteStoppedReason routeStoppedReason) {
            Toast.makeText(context, "Navigation ended", Toast.LENGTH_LONG).show();
        }
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
