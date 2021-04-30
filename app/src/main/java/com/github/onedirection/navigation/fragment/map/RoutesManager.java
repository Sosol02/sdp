package com.github.onedirection.navigation.fragment.map;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.github.onedirection.BuildConfig;
import com.github.onedirection.utils.EspressoIdlingResource;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteLeg;
import com.mapquest.navigation.model.RouteOptionType;
import com.mapquest.navigation.model.RouteOptions;
import com.mapquest.navigation.model.SystemOfMeasurement;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Destination;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.opencensus.internal.DefaultVisibilityForTesting;

public class RoutesManager {

    private LineManager lineManager;
    private RouteService routeService;
    private List<Route> routes;
    private List<Line> lines;

    private final String LINE_COLOR = "Blue";
    private final float LINE_WIDTH = 2f;

    public RoutesManager(Context context, MapView mapView, MapboxMap mapboxMap, Style style) {
        this.lineManager = new LineManager(mapView, mapboxMap, style);
        this.routeService = new RouteService.Builder().build(context,
                BuildConfig.API_KEY);
    }

    public void findRoute(LatLng start, LatLng finish) {
        findRoute(start, finish, () -> {});
    }

    @VisibleForTesting
    public void findRoute(LatLng start, LatLng finish, Runnable runnableOnSuccess) {
        clearRoute();
        clearLines();
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
                if (routes.size() > 0) {
                    routes = routes1;
                    displayRoute(routes.get(0));
                }
                runnableOnSuccess.run();
            }

            @Override
            public void onRequestFailed(@Nullable Integer httpStatusCode, @Nullable IOException exception) {}

            @Override
            public void onRequestMade() {}
        });
    }

    private void clearRoute() {
        routes = null;
    }

    private void clearLines() {
        lines = null;
        lineManager.deleteAll();
    }

    public void clearRoutesManager() {
        clearLines();
        clearRoute();
    }

    private void displayRoute(Route selectedRoute) {
        clearLines();
        if (routes == null) {
            throw new IllegalStateException("Routes should't be empty when trying to display a route");
        }

        if (!routes.contains(selectedRoute)) {
            throw new IllegalStateException("Selected route should be in routes");
        }

        lines = createLinesRoute(selectedRoute.getLegs());
    }

    private List<Line> createLinesRoute(List<RouteLeg> routeLegs) {
        if (routeLegs == null) {
            throw new IllegalArgumentException("RoutesLegs should not be null");
        }
        List<Line> linesRoute = new ArrayList<>();
        for (RouteLeg routeLeg: routes.get(0).getLegs()) {
            List<Coordinate> coordinates = routeLeg.getShape().getCoordinates();
            List<LatLng> latLngs = coordinates.stream().map(coordinate ->
                    new LatLng(coordinate.getLatitude(), coordinate.getLongitude()))
                    .collect(Collectors.toList());
            linesRoute.add(createLineRoute(latLngs));
        }
        return linesRoute;
    }

    private Line createLineRoute(List<LatLng> latLngs) {
        LineOptions lineOptions = new LineOptions()
                .withLatLngs(latLngs)
                .withLineWidth(LINE_WIDTH)
                .withLineColor(LINE_COLOR);
        return lineManager.create(lineOptions);
    }

}
