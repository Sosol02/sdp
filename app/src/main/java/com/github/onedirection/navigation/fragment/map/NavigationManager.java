package com.github.onedirection.navigation.fragment.map;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.github.onedirection.BuildConfig;
import com.github.onedirection.R;
import com.github.onedirection.geolocation.location.DeviceLocationProvider;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapquest.navigation.internal.collection.CollectionsUtil;
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
import com.mapquest.navigation.model.SpeedLimitSpan;
import com.mapquest.navigation.model.Traffic;
import com.mapquest.navigation.model.UserLocationTrackingConsentStatus;
import com.mapquest.navigation.model.location.Destination;
import com.mapquest.navigation.model.location.Location;
import com.mapquest.navigation.model.location.LocationObservation;
import com.mapquest.navigation.NavigationManager.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * NavigationManager is used with mapquest to be able to navigate between events on the map
 * in mapfragment by giving it a route
 * Class inspired from the example provided by mapquest :
 * https://github.com/MapQuest/mq-navigation-sdk-reference-app-android
 */
public class NavigationManager {

    private final com.mapquest.navigation.NavigationManager navigationManager;
    private final RouteDisplayManager routeDisplayManager;
    private final MapboxMap mapboxMap;
    private final Context context;

    private final CenteringMapOnLocationProgressListener centeringMapOnLocationProgressListener;
    private final RouteUpdatingRerouteListener routeUpdatingRerouteListener;
    private final ToastUpdateNavigationStateListener toastUpdateNavigationStateListener;
    private final UpdatingNavigationProgressListener updatingNavigationProgressListener;
    private final UpdatingSpeedLimitSpanListener updatingSpeedLimitSpanListener;
    private final EtaUpdateResponseListener etaUpdateResponseListener;

    private final RelativeLayout maneuverBar;
    private final ImageView nextManeuverIcon; //TODO change label with upcoming maneuver
    private final TextView nextManeuverDistance;
    private final TextView nextManeuver;

    private final RelativeLayout arrivalBar;
    private final TextView timeNextDestination;
    private final TextView timeFinalDestination;
    private final TextView remainingDistance;

    private final AppCompatImageButton myLocationButton;

    private static final double NAVIGATION_ZOOM = 16;
    private static final double NAVIGATION_TILT_VALUE_DEGREES = 60;
    private static final double ON_EXIT_NAVIGATION_ZOOM = 20;
    private static final double ON_EXIT_NAVIGATION_TILT_VALUE_DEGREES = 0;
    private static final int MAPBOX_CAMERA_ANIMATION_DURATION = 1000;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("h:mm a", Locale.ROOT);

    public NavigationManager(Context context, DeviceLocationProvider deviceLocationProvider,
                             MapboxMap mapboxMap, RouteDisplayManager routeDisplayManager, View view) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(deviceLocationProvider);
        Objects.requireNonNull(routeDisplayManager);
        this.mapboxMap = mapboxMap;
        this.context = context;
        this.routeDisplayManager = routeDisplayManager;

        navigationManager = new com.mapquest.navigation.NavigationManager.Builder(context,
                BuildConfig.API_KEY, new DeviceLocationProviderAdapter(deviceLocationProvider))
                .build();
        navigationManager.setRerouteBehaviorOverride(coordinate -> true);

        routeUpdatingRerouteListener = new RouteUpdatingRerouteListener();
        centeringMapOnLocationProgressListener = new CenteringMapOnLocationProgressListener();
        toastUpdateNavigationStateListener = new ToastUpdateNavigationStateListener();
        updatingNavigationProgressListener = new UpdatingNavigationProgressListener();
        updatingSpeedLimitSpanListener = new UpdatingSpeedLimitSpanListener();
        etaUpdateResponseListener = new EtaUpdateResponseListener();

        myLocationButton = view.findViewById(R.id.my_location_button);
        maneuverBar = view.findViewById(R.id.maneuverBarLayout);
        arrivalBar = view.findViewById(R.id.arrivalBarLayout);
        maneuverBar.setVisibility(View.GONE);
        arrivalBar.setVisibility(View.GONE);

        nextManeuverIcon = view.findViewById(R.id.next_maneuver_icon);
        nextManeuverDistance = view.findViewById(R.id.next_maneuver_distance);
        nextManeuver = view.findViewById(R.id.next_maneuver);
        timeNextDestination = view.findViewById(R.id.eta_next_destination);
        timeFinalDestination = view.findViewById(R.id.eta_final_destination);
        remainingDistance = view.findViewById(R.id.remaining_distance);
        view.findViewById(R.id.stop).setOnClickListener(v -> {
            if (navigationManager.getNavigationState() == NavigationState.ACTIVE) {
                stopNavigation();
            }
        });
    }

    public void startNavigation(@NonNull Route route) {
        if (navigationManager.getNavigationState() == NavigationState.ACTIVE) {
            throw new IllegalStateException("You cannot start the navigation manager when it has already started");
        }
        Objects.requireNonNull(route);
        navigationManager.initialize();
        navigationManager.setUserLocationTrackingConsentStatus(UserLocationTrackingConsentStatus.DENIED);
        navigationManager.addProgressListener(centeringMapOnLocationProgressListener);
        navigationManager.addRerouteListener(routeUpdatingRerouteListener);
        navigationManager.addNavigationStateListener(toastUpdateNavigationStateListener);
        navigationManager.addProgressListener(updatingNavigationProgressListener);
        navigationManager.addEtaResponseListener(etaUpdateResponseListener);
        navigationManager.addSpeedLimitSpanListener(updatingSpeedLimitSpanListener);

        CameraPosition cameraPosition = new CameraPosition.Builder(mapboxMap.getCameraPosition())
                .zoom(NAVIGATION_ZOOM)
                .tilt(NAVIGATION_TILT_VALUE_DEGREES)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), MAPBOX_CAMERA_ANIMATION_DURATION);
        myLocationButton.setVisibility(View.GONE);
        maneuverBar.setVisibility(View.VISIBLE);
        arrivalBar.setVisibility(View.VISIBLE);

        navigationManager.startNavigation(route);
    }

    public void stopNavigation() {
        if (navigationManager.getNavigationState() == NavigationState.STOPPED) {
            throw new IllegalStateException("You cannot stop the navigation manager when it has not started");
        }
        navigationManager.cancelNavigation();

        routeDisplayManager.clearDisplayedRoute();
        CameraPosition cameraPosition = new CameraPosition.Builder(mapboxMap.getCameraPosition())
                .zoom(ON_EXIT_NAVIGATION_ZOOM)
                .tilt(ON_EXIT_NAVIGATION_TILT_VALUE_DEGREES)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), MAPBOX_CAMERA_ANIMATION_DURATION);
        myLocationButton.setVisibility(View.VISIBLE);
        maneuverBar.setVisibility(View.GONE);
        arrivalBar.setVisibility(View.GONE);

        navigationManager.removeProgressListener(centeringMapOnLocationProgressListener);
        navigationManager.removeRerouteListener(routeUpdatingRerouteListener);
        navigationManager.removeNavigationStateListener(toastUpdateNavigationStateListener);
        navigationManager.removeProgressListener(updatingNavigationProgressListener);
        navigationManager.removeEtaResponseListener(etaUpdateResponseListener);
        navigationManager.removeSpeedLimitSpanListener(updatingSpeedLimitSpanListener);
        navigationManager.deinitialize();
    }

    private void updateUiOnStartAndReroute(Route route) {
        routeDisplayManager.displayRoute(route);
        RouteLeg lastRouteLeg = CollectionsUtil.lastValue(route.getLegs());
        long timeForNextDestination = navigationManager.getCurrentRouteLeg().getTraffic().getEstimatedTimeOfArrival().getTime();
        long timeForFinalDestination = lastRouteLeg.getTraffic().getEstimatedTimeOfArrival().getTime();

        timeNextDestination.setText(DATE_FORMAT.format(new Date(timeForNextDestination)));
        timeFinalDestination.setText(DATE_FORMAT.format(new Date(timeForFinalDestination)));
    }

    private class ToastUpdateNavigationStateListener implements NavigationStateListener {

        @Override
        public void onNavigationStarted() {
            Toast.makeText(context, R.string.navigation_started_text, Toast.LENGTH_LONG).show();
            updateUiOnStartAndReroute(navigationManager.getRoute());
        }

        @Override
        public void onNavigationStopped(@NonNull RouteStoppedReason routeStoppedReason) {
            Toast.makeText(context, R.string.navigation_stopped_text, Toast.LENGTH_LONG).show();
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
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), MAPBOX_CAMERA_ANIMATION_DURATION);
        }
    }

    private class RouteUpdatingRerouteListener implements RerouteListener {

        @Override
        public void onRerouteWouldOccur(Location location) {}

        @Override
        public void onRerouteRequested(Location location) {
            Toast.makeText(context, R.string.navigation_reroute_request, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRerouteReceived(Route route) {
            Toast.makeText(context, R.string.navigation_reroute_success, Toast.LENGTH_LONG).show();
            routeDisplayManager.displayRoute(route);
            updateUiOnStartAndReroute(route);
        }

        @Override
        public void onRerouteFailed() {
            Toast.makeText(context, R.string.navigation_reroute_failed, Toast.LENGTH_LONG).show();
        }
    }

    private class UpdatingNavigationProgressListener implements NavigationProgressListener {

        @Override
        public void onLocationObservationReceived(@NonNull LocationObservation locationObservation) {
            remainingDistance.setText((int) locationObservation.getRemainingLegDistance() + " " + context.getResources().getString(R.string.navigation_distance_unit));
            nextManeuverDistance.setText(String.format(Locale.getDefault(), context.getResources().getString(R.string.navigation_distance_format), locationObservation
                                                        .getDistanceToUpcomingManeuver().intValue()));
        }

        @Override
        public void onUpcomingManeuverUpdated(@Nullable Maneuver maneuver) {
            String labelText = ((maneuver.getName() != null) && !maneuver.getName().trim().isEmpty()) ?
                    maneuver.getTypeText() + ", " + maneuver.getName() :
                    maneuver.getTypeText();
            nextManeuver.setText(labelText);
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
            /*for (SpeedLimitSpan speedLimitSpan : enteredSpeedLimits) {
                float speed = speedLimitSpan.getSpeedLimit().getSpeed();
                SpeedLimit.Type type = speedLimitSpan.getSpeedLimit().getType();
                //TODO update UI
            }*/
        }
    }

    private class EtaUpdateResponseListener implements EtaResponseListener {

        @Override
        public void onEtaUpdate(@NonNull EstimatedTimeOfArrival estimatedTimeOfArrival) {
            long timeLeft = estimatedTimeOfArrival.getTime();
        }

        @Override
        public void onEtaUpdate(@NonNull Map<String, Traffic> map, String s) {
            String lastRouteLegKey = Integer.toString(CollectionsUtil.lastIndex(navigationManager.getRoute().getLegs()));
            long timeForNextDestination = map.get(s).getEstimatedTimeOfArrival().getTime();
            long timeForFinalDestination = map.get(lastRouteLegKey).getEstimatedTimeOfArrival().getTime();

            timeNextDestination.setText(DATE_FORMAT.format(new Date(timeForNextDestination)));
            timeFinalDestination.setText(DATE_FORMAT.format(new Date(timeForFinalDestination)));
        }

        @Override
        public void onRequestFailed(@Nullable Integer integer, @Nullable IOException e) {}

        @Override
        public void onRequestMade() {}
    }

}
