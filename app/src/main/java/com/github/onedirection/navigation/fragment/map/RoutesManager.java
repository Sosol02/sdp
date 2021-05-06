package com.github.onedirection.navigation.fragment.map;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.github.onedirection.BuildConfig;
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
import java.util.Objects;

public class RoutesManager {

    private RouteService routeService;
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
                .systemOfMeasurementForDisplayText(SystemOfMeasurement.UNITED_STATES_CUSTOMARY) // or specify METRIC
                .language("en_US") // NOTE: alternately, specify "es_US" for Spanish in the US
                .highways(RouteOptionType.ALLOW)
                .tolls(RouteOptionType.ALLOW)
                .ferries(RouteOptionType.DISALLOW)
                .internationalBorders(RouteOptionType.DISALLOW)
                .unpaved(RouteOptionType.DISALLOW)
                .seasonalClosures(RouteOptionType.AVOID)
                .build();

        routeService.requestRoutes(from, to, routeOptions, new RoutesResponseListener() {
            @Override
            public void onRoutesRetrieved(List<Route> routes1) {
                if (routes1.size() > 0) {
                    routes = routes1;
                }
                routesResponseListener.onRoutesRetrieved(routes1);
            }

            @Override
            public void onRequestFailed(@Nullable Integer httpStatusCode, @Nullable IOException exception) {
                routesResponseListener.onRequestFailed(httpStatusCode, exception);
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
