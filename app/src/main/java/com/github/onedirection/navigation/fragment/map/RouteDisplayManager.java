package com.github.onedirection.navigation.fragment.map;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteLeg;
import com.mapquest.navigation.model.location.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * RouteDisplayManager is used with mapbox to display a route between events in the form of lines
 * on the map of mapfragment
 */
public class RouteDisplayManager {

    private final LineManager lineManager;
    private List<Line> lines;

    private static final String LINE_COLOR = "Blue";
    private static final float LINE_WIDTH = 10f;

    public RouteDisplayManager(MapView mapView, MapboxMap mapboxMap, Style style) {
        this.lineManager = new LineManager(mapView, mapboxMap, style);
    }

    public void displayRoute(Route route) {
        Objects.requireNonNull(route);
        clearDisplayedRoute();
        lines = createLinesRoute(route.getLegs());
    }

    private List<Line> createLinesRoute(List<RouteLeg> routeLegs) {
        List<Line> linesRoute = new ArrayList<>();
        for (RouteLeg routeLeg: routeLegs) {
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

    public void clearDisplayedRoute() {
        lines = null;
        lineManager.deleteAll();
    }
}
