package com.github.onedirection.navigation.fragment.map;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.onedirection.BuildConfig;
import com.github.onedirection.geolocation.location.DeviceLocationProvider;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapquest.navigation.listener.DefaultNavigationProgressListener;
import com.mapquest.navigation.listener.EtaResponseListener;
import com.mapquest.navigation.listener.NavigationProgressListener;
import com.mapquest.navigation.listener.NavigationStateListener;
import com.mapquest.navigation.listener.RerouteListener;
import com.mapquest.navigation.listener.SpeedLimitSpanListener;
import com.mapquest.navigation.model.EstimatedTimeOfArrival;
import com.mapquest.navigation.model.Maneuver;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteLeg;
import com.mapquest.navigation.model.RouteStoppedReason;
import com.mapquest.navigation.model.SpeedLimit;
import com.mapquest.navigation.model.SpeedLimitSpan;
import com.mapquest.navigation.model.Traffic;
import com.mapquest.navigation.model.UserLocationTrackingConsentStatus;
import com.mapquest.navigation.model.location.Destination;
import com.mapquest.navigation.model.location.Location;
import com.mapquest.navigation.model.location.LocationObservation;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private final ToastUpdateNavigationStateListener toastUpdateNavigationStateListener;
    private final UpdatingSpeedLimitSpanListener updatingSpeedLimitSpanListener;
    private final EtaUpdateResponseListener etaUpdateResponseListener;

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
        toastUpdateNavigationStateListener = new ToastUpdateNavigationStateListener();
        updatingSpeedLimitSpanListener = new UpdatingSpeedLimitSpanListener();
        etaUpdateResponseListener = new EtaUpdateResponseListener();
    }

    public void startNavigation(@NonNull Route route) {
        navigationManager.initialize();
        navigationManager.setUserLocationTrackingConsentStatus(UserLocationTrackingConsentStatus.DENIED);
        navigationManager.addProgressListener(centeringMapOnLocationProgressListener);
        navigationManager.addRerouteListener(routeUpdatingRerouteListener);
        navigationManager.addNavigationStateListener(toastUpdateNavigationStateListener);
        navigationManager.addEtaResponseListener(etaUpdateResponseListener);
        navigationManager.addSpeedLimitSpanListener(updatingSpeedLimitSpanListener);
        navigationManager.startNavigation(route);
    }

    public void stopNavigation() {
        navigationManager.cancelNavigation();
        navigationManager.removeProgressListener(centeringMapOnLocationProgressListener);
        navigationManager.removeRerouteListener(routeUpdatingRerouteListener);
        navigationManager.removeNavigationStateListener(toastUpdateNavigationStateListener);
        navigationManager.removeEtaResponseListener(etaUpdateResponseListener);
        navigationManager.removeSpeedLimitSpanListener(updatingSpeedLimitSpanListener);
        navigationManager.deinitialize();
    }

    private class ToastUpdateNavigationStateListener implements NavigationStateListener {

        @Override
        public void onNavigationStarted() {
            Toast.makeText(context, "Navigation started", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNavigationStopped(@NonNull RouteStoppedReason routeStoppedReason) {
            Toast.makeText(context, "Navigation ended", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNavigationPaused() {}

        @Override
        public void onNavigationResumed() {}
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
        public void onRerouteWouldOccur(Location location) {}

        @Override
        public void onRerouteRequested(Location location) {
            Toast.makeText(context, "Reroute request", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRerouteReceived(Route route) {
            Toast.makeText(context, "Reroute succeeded", Toast.LENGTH_LONG).show();
            routeDisplayManager.displayRoute(route);
        }

        @Override
        public void onRerouteFailed() {
            Toast.makeText(context, "Reroute failed", Toast.LENGTH_LONG).show();
        }
    }

    private class UpdatingNavigationProgressListener implements NavigationProgressListener{

        @Override
        public void onLocationObservationReceived(@NonNull LocationObservation locationObservation) {

        }

        @Override
        public void onUpcomingManeuverUpdated(@Nullable Maneuver maneuver) {

        }

        @Override
        public void onInaccurateObservationReceived(Location location) {}

        @Override
        public void onDestinationReached(@NonNull Destination destination, boolean finalDestination, @NonNull RouteLeg routeLeg,
                                         @NonNull DestinationAcceptanceHandler destinationAcceptanceHandler) {

        }
    }

    private class UpdatingSpeedLimitSpanListener implements SpeedLimitSpanListener {

        @Override
        public void onSpeedLimitBoundariesCrossed(@NonNull Set<SpeedLimitSpan> exitedSpeedLimits, @NonNull Set<SpeedLimitSpan> enteredSpeedLimits) {
            for (SpeedLimitSpan speedLimitSpan : enteredSpeedLimits) {
                float speed = speedLimitSpan.getSpeedLimit().getSpeed();
                SpeedLimit.Type type = speedLimitSpan.getSpeedLimit().getType();
            }
        }
    }

    private class EtaUpdateResponseListener implements EtaResponseListener {

        @Override
        public void onEtaUpdate(@NonNull EstimatedTimeOfArrival estimatedTimeOfArrival) {
            long timeLeft = estimatedTimeOfArrival.getTime();
        }

        @Override
        public void onEtaUpdate(@NonNull Map<String, Traffic> map, String s) {}

        @Override
        public void onRequestFailed(@Nullable Integer integer, @Nullable IOException e) {}

        @Override
        public void onRequestMade() {}
    }

}
