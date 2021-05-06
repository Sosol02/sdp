package com.github.onedirection.navigation.fragment.map;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.onedirection.BuildConfig;
import com.github.onedirection.utils.EspressoIdlingResource;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteOptionType;
import com.mapquest.navigation.model.RouteOptions;
import com.mapquest.navigation.model.SystemOfMeasurement;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Destination;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * RoutesManager is use with mapquest to able to query a route between events
 */
public class RoutesManager {

    private final RouteService routeService;
    private List<Route> routes;

    public RoutesManager(Context context) {
        Objects.requireNonNull(context);
        this.routeService = new RouteService.Builder().build(context,
                BuildConfig.API_KEY);
    }

    public void findRoute(LatLng start, LatLng finish) {
        findRoute(start, finish, new RoutesResponseListener() {
            @Override
            public void onRoutesRetrieved(@NonNull List<Route> list) {}

            @Override
            public void onRequestFailed(@Nullable Integer integer, @Nullable IOException e) {}

            @Override
            public void onRequestMade() {}
        });
    }

    public void findRoute(LatLng start, LatLng finish, RoutesResponseListener routesResponseListener) {
        clearRoutes();
        Coordinate from = new Coordinate(start.getLatitude(), start.getLongitude());
        Coordinate tmp = new Coordinate(finish.getLatitude(), finish.getLongitude());
        List<Destination> to = Arrays.asList(new Destination(tmp, null));

        RouteOptions routeOptions = new RouteOptions.Builder()
                .maxRoutes(3)
                .systemOfMeasurementForDisplayText(SystemOfMeasurement.METRIC)
                .language("en_US") // TODO try to input the system language
                .highways(RouteOptionType.ALLOW)
                .tolls(RouteOptionType.ALLOW)
                .ferries(RouteOptionType.AVOID)
                .internationalBorders(RouteOptionType.ALLOW)
                .unpaved(RouteOptionType.AVOID)
                .seasonalClosures(RouteOptionType.AVOID)
                .build();

        EspressoIdlingResource.getInstance().lockIdlingResource();
        routeService.requestRoutes(from, to, routeOptions, new RoutesResponseListener() {
            @Override
            public void onRoutesRetrieved(List<Route> routes1) {
                if (routes1.size() > 0) {
                    routes = routes1;
                }
                routesResponseListener.onRoutesRetrieved(routes1);
                EspressoIdlingResource.getInstance().unlockIdlingResource();
            }

            @Override
            public void onRequestFailed(@Nullable Integer httpStatusCode, @Nullable IOException exception) {
                routesResponseListener.onRequestFailed(httpStatusCode, exception);
                EspressoIdlingResource.getInstance().unlockIdlingResource();
            }

            @Override
            public void onRequestMade() {
                routesResponseListener.onRequestMade();
            }
        });
    }

    public void clearRoutes() {
        routes = null;
    }
}
