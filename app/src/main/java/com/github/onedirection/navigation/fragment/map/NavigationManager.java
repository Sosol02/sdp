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
import com.mapquest.navigation.internal.unit.Speed;
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
import com.mapquest.navigation.NavigationManager.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private final ImageView nextManeuverIcon;
    private final TextView nextManeuverDistance;
    private final TextView nextManeuver;

    private final RelativeLayout arrivalBar;
    private final TextView timeNextDestination;
    private final TextView timeFinalDestination;
    private final TextView remainingDistance;
    private final ImageView speedLimitBlankSign;
    private final TextView speedLimitValue;

    private final AppCompatImageButton myLocationButton;

    private static final double NAVIGATION_ZOOM = 18;
    private static final double NAVIGATION_TILT_VALUE_DEGREES = 55;
    private static final double ON_EXIT_NAVIGATION_ZOOM = 20;
    private static final double ON_EXIT_NAVIGATION_TILT_VALUE_DEGREES = 0;
    private static final int MAPBOX_CAMERA_ANIMATION_DURATION = 1000;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("kk:mm", Locale.ROOT);
    private static final Map<Maneuver.Type, Integer> MANEUVER_RESOURCES_ID_BY_TYPE = buildManeuverIconResources();

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
        speedLimitBlankSign = view.findViewById(R.id.speed_limit_blank);
        speedLimitValue = view.findViewById(R.id.speed_limit);

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

    private void updateUiOnStartAndReroute(@NonNull Route route) {
        routeDisplayManager.displayRoute(route);

        List<SpeedLimitSpan> speedLimitSpans = route.getLeg(0).getFeatures().getMaximumSpeedLimits();
        Set<SpeedLimitSpan> speedLimitSpanSet = new HashSet<>(speedLimitSpans);
        updateUiMaxSpeed(speedLimitSpanSet);

        RouteLeg lastRouteLeg = CollectionsUtil.lastValue(route.getLegs());
        long timeForNextDestination = navigationManager.getCurrentRouteLeg().getTraffic().getEstimatedTimeOfArrival().getTime();
        long timeForFinalDestination = lastRouteLeg.getTraffic().getEstimatedTimeOfArrival().getTime();

        String lastRouteLegKey = Integer.toString(CollectionsUtil.lastIndex(navigationManager.getRoute().getLegs()));
        String currentRouteLegKey = Integer.toString(navigationManager.getRoute().getLegs().indexOf(navigationManager.getCurrentRouteLeg()));
        updateUiTime(timeForNextDestination, timeForFinalDestination, lastRouteLegKey.equals(currentRouteLegKey));
    }

    private void updateUiTime(long timeForNextDestination, long timeForFinalDestination, boolean isTheSameTime) {
        if (timeForNextDestination == -1 || timeForFinalDestination == -1) {
            timeNextDestination.setVisibility(View.INVISIBLE);
            timeFinalDestination.setVisibility(View.INVISIBLE);
        } else {
            timeNextDestination.setVisibility(View.VISIBLE);
            timeFinalDestination.setVisibility(View.VISIBLE);
        }
        timeNextDestination.setText(DATE_FORMAT.format(new Date(timeForNextDestination)));
        if (!isTheSameTime) {
            timeFinalDestination.setText(DATE_FORMAT.format(new Date(timeForFinalDestination)));
        }
    }

    private void updateUiMaxSpeed(Set<SpeedLimitSpan> speedLimitSpans) {
        if (speedLimitSpans != null) {
            boolean foundMaximumSpeed = false;
            for (SpeedLimitSpan speedLimitSpan : speedLimitSpans) {
                if (speedLimitSpan.getSpeedLimit().getType() == SpeedLimit.Type.MAXIMUM) {
                    foundMaximumSpeed = true;
                    speedLimitValue.setText(Math.round(Speed.metersPerSecond(speedLimitSpan.getSpeedLimit().getSpeed())
                            .toKilometersPerHour()));
                }
            }
            speedLimitBlankSign.setVisibility(foundMaximumSpeed ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private String getStringDistanceFromIntDistance(int distance) {
        if (distance > 5000) {
            double distanceKm = ((double) distance) / 1000;
            BigDecimal b = new BigDecimal(Double.toString(distanceKm));
            b.setScale(1, RoundingMode.CEILING);
            return b.toString() + " " + context.getResources().getString(R.string.navigation_distance_big_unit);
        } else {
            return distance + " " + context.getResources().getString(R.string.navigation_distance_small_unit);
        }
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
            Toast.makeText(context, R.string.navigation_reroute_request, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRerouteReceived(Route route) {
            Toast.makeText(context, R.string.navigation_reroute_success, Toast.LENGTH_SHORT).show();
            if (route != null) {
                routeDisplayManager.displayRoute(route);
                updateUiOnStartAndReroute(route);
            }
        }

        @Override
        public void onRerouteFailed() {
            Toast.makeText(context, R.string.navigation_reroute_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private class UpdatingNavigationProgressListener implements NavigationProgressListener {

        @Override
        public void onLocationObservationReceived(@NonNull LocationObservation locationObservation) {

            remainingDistance.setText(getStringDistanceFromIntDistance((int) locationObservation.getRemainingLegDistance()));
            if (locationObservation.getDistanceToUpcomingManeuver() != null) {
                nextManeuverDistance.setText(getStringDistanceFromIntDistance(locationObservation.
                        getDistanceToUpcomingManeuver().intValue()));
            }
        }

        @Override
        public void onUpcomingManeuverUpdated(@Nullable Maneuver maneuver) {
            if (maneuver != null) {
                String labelText = (!maneuver.getName().trim().isEmpty()) ?
                        maneuver.getTypeText() + ", " + maneuver.getName() :
                        maneuver.getTypeText();
                nextManeuver.setText(labelText);
                if (MANEUVER_RESOURCES_ID_BY_TYPE.containsKey(maneuver.getType())) {
                    nextManeuverIcon.setImageResource(MANEUVER_RESOURCES_ID_BY_TYPE.get(maneuver.getType()));
                    nextManeuverIcon.setVisibility(View.VISIBLE);
                }
            } else {
                nextManeuverIcon.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onInaccurateObservationReceived(Location location) {}

        @Override
        public void onDestinationReached(@NonNull Destination destination, boolean finalDestination, @NonNull RouteLeg routeLeg,
                                         @NonNull DestinationAcceptanceHandler destinationAcceptanceHandler) {
            if (finalDestination) {
                Toast.makeText(context, "You have arrived to your destination", Toast.LENGTH_LONG).show();
                //stopNavigation();
            } else {
                Toast.makeText(context, "You have arrived at a way point", Toast.LENGTH_LONG).show();
            }
            destinationAcceptanceHandler.confirmArrival(true);

        }
    }

    private class UpdatingSpeedLimitSpanListener implements SpeedLimitSpanListener {

        @Override
        public void onSpeedLimitBoundariesCrossed(@NonNull Set<SpeedLimitSpan> exitedSpeedLimits, @NonNull Set<SpeedLimitSpan> enteredSpeedLimits) {
            updateUiMaxSpeed(enteredSpeedLimits);
        }
    }

    private class EtaUpdateResponseListener implements EtaResponseListener {

        @Override
        public void onEtaUpdate(@NonNull EstimatedTimeOfArrival estimatedTimeOfArrival) {}

        @Override
        public void onEtaUpdate(@NonNull Map<String, Traffic> map, String s) {
            if (navigationManager.getRoute() != null) {
                String lastRouteLegKey = Integer.toString(CollectionsUtil.lastIndex(navigationManager.getRoute().getLegs()));
                long timeForNextDestination = -1;
                if (map.containsKey(s)) {
                   timeForNextDestination = map.get(s).getEstimatedTimeOfArrival().getTime();
                }
                long timeForFinalDestination = -1;
                if (map.containsKey(lastRouteLegKey)) {
                    timeForFinalDestination = map.get(lastRouteLegKey).getEstimatedTimeOfArrival().getTime();
                }
                updateUiTime(timeForNextDestination, timeForFinalDestination, s.equals(lastRouteLegKey));
            }
        }

        @Override
        public void onRequestFailed(@Nullable Integer integer, @Nullable IOException e) {}

        @Override
        public void onRequestMade() {}
    }


    private static Map<Maneuver.Type, Integer> buildManeuverIconResources() {
        Map<Maneuver.Type,Integer> mapManeuverIdByType = new HashMap<>();

        mapManeuverIdByType.put(Maneuver.Type.LEFT_UTURN, R.drawable.maneuver_icon_uturn_left);
        mapManeuverIdByType.put(Maneuver.Type.SHARP_LEFT, R.drawable.maneuver_icon_sharp_left);
        mapManeuverIdByType.put(Maneuver.Type.LEFT, R.drawable.maneuver_icon_left);
        mapManeuverIdByType.put(Maneuver.Type.SLIGHT_LEFT, R.drawable.maneuver_icon_slight_left);
        mapManeuverIdByType.put(Maneuver.Type.STRAIGHT, R.drawable.maneuver_icon_straight);
        mapManeuverIdByType.put(Maneuver.Type.SLIGHT_RIGHT, R.drawable.maneuver_icon_slight_right);
        mapManeuverIdByType.put(Maneuver.Type.RIGHT, R.drawable.maneuver_icon_right);
        mapManeuverIdByType.put(Maneuver.Type.SHARP_RIGHT, R.drawable.maneuver_icon_sharp_right);
        mapManeuverIdByType.put(Maneuver.Type.RIGHT_UTURN, R.drawable.maneuver_icon_uturn_right);
        mapManeuverIdByType.put(Maneuver.Type.MERGE, R.drawable.maneuver_icon_merge);
        mapManeuverIdByType.put(Maneuver.Type.LEFT_MERGE, R.drawable.maneuver_icon_merge_left);
        mapManeuverIdByType.put(Maneuver.Type.RIGHT_MERGE, R.drawable.maneuver_icon_merge_right);
        mapManeuverIdByType.put(Maneuver.Type.LEFT_OFF_RAMP, R.drawable.maneuver_icon_exit_left);
        mapManeuverIdByType.put(Maneuver.Type.RIGHT_OFF_RAMP, R.drawable.maneuver_icon_exit_right);
        mapManeuverIdByType.put(Maneuver.Type.LEFT_FORK, R.drawable.maneuver_icon_fork_left);
        mapManeuverIdByType.put(Maneuver.Type.RIGHT_FORK, R.drawable.maneuver_icon_fork_right);

        return Collections.unmodifiableMap(mapManeuverIdByType);
    }
}
