package com.github.onedirection.map;

import androidx.annotation.NonNull;

import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.dataclient.listener.RouteSummaryResponseListener;
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.model.EstimatedTimeOfArrival;
import com.mapquest.navigation.model.Features;
import com.mapquest.navigation.model.Instruction;
import com.mapquest.navigation.model.Maneuver;
import com.mapquest.navigation.model.Prompt;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteLeg;
import com.mapquest.navigation.model.RouteOptionType;
import com.mapquest.navigation.model.RouteOptions;
import com.mapquest.navigation.model.RouteSummaryOptions;
import com.mapquest.navigation.model.Shape;
import com.mapquest.navigation.model.SpeedLimit;
import com.mapquest.navigation.model.SpeedLimitSpan;
import com.mapquest.navigation.model.SystemOfMeasurement;
import com.mapquest.navigation.model.Traffic;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Destination;

import java.util.ArrayList;
import java.util.List;

/**
 *    Route service that return route between those points
 *    LatLng(40.7326808, -73.9843407);
 *    LatLng(40.7326808, -73.9843407);
 */

public class RouteServiceMock implements RouteService {


    @Override
    public void requestRoutes(@NonNull Coordinate coordinate, @NonNull List<Destination> list, @NonNull RouteOptions routeOptions, @NonNull RoutesResponseListener routesResponseListener) {
        Coordinate startCoordinate = new Coordinate(40.7326808, -73.9843407);
        List<Destination> destinations = new ArrayList<>();
        Coordinate coordinate1 = new Coordinate(40.7326808, -73.9843407);
        destinations.add(new Destination(coordinate1, null));
        List<RouteLeg> routeLegs = new ArrayList<>();
        double [] coordinates = {40.73286, -73.98459000000001, 40.73286, -73.98459000000001};
        List<Coordinate> coordinates1 = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i += 2) {
            coordinates1.add(new Coordinate(coordinates[i], coordinates[i + 1]));
        }
        Shape shape = new Shape(coordinates1);
        List<Maneuver> maneuvers = new ArrayList<>();
        maneuvers.add(new Maneuver(0.0, Maneuver.Type.START, "Start", "2nd Ave", new ArrayList<>()));
        maneuvers.add(new Maneuver(1.0, Maneuver.Type.DESTINATION, "Destination", "2nd Ave", new ArrayList<>()));
        List<Prompt> prompts = new ArrayList<>();
        prompts.add(new Prompt(0.0, Prompt.Placement.AFTER, "Go northeast on 2nd Avenue", "Go northeast on 2nd Avenue", 0, 0));
        prompts.add(new Prompt(1.0, Prompt.Placement.BEFORE, "You have arrived at your destination", "You have arrived at your destination", 2, 20));
        List<SpeedLimitSpan> speedLimits = new ArrayList<>();
        speedLimits.add(new SpeedLimitSpan(0.0, 1.0, (float) 11.111111, SpeedLimit.Type.MAXIMUM, SystemOfMeasurement.UNITED_STATES_CUSTOMARY));
        List<Instruction> instructions = new ArrayList<>();
        instructions.add(new Instruction("Start out going northeast on 2nd Ave", 0.0, Maneuver.Type.START, (float) 0.0));
        instructions.add(new Instruction("You have arrived at your destination", 1.0, Maneuver.Type.DESTINATION, (float) 0.0));
        routeLegs.add(new RouteLeg("0", 0.0, shape, maneuvers, prompts,
                new Features(speedLimits), instructions, new Traffic(new ArrayList<>(), new EstimatedTimeOfArrival(0))));
        RouteOptions routeOptions1 = new RouteOptions.Builder()
                .maxRoutes(3)
                .systemOfMeasurementForDisplayText(SystemOfMeasurement.METRIC)
                .language("en_US")
                .highways(RouteOptionType.ALLOW)
                .tolls(RouteOptionType.ALLOW)
                .ferries(RouteOptionType.AVOID)
                .internationalBorders(RouteOptionType.ALLOW)
                .unpaved(RouteOptionType.AVOID)
                .seasonalClosures(RouteOptionType.AVOID)
                .build();
        Route route = new Route("60956967-0379-6750-02b4-32bb-0ac6d336ed83", routeLegs,
                Route.TrafficOverview.LIGHT, routeOptions, startCoordinate, destinations, "");
        List<Route> listRoute = new ArrayList<>();
        listRoute.add(route);
        routesResponseListener.onRoutesRetrieved(listRoute);
    }

    @Override
    public void requestRoutesForCoordinates(@NonNull Coordinate coordinate, @NonNull List<Coordinate> list, @NonNull RouteOptions routeOptions, @NonNull RoutesResponseListener routesResponseListener) {

    }

    @Override
    public void getRouteSummary(@NonNull Coordinate coordinate, @NonNull Destination destination, @NonNull RouteSummaryResponseListener routeSummaryResponseListener) {

    }

    @Override
    public void getRouteSummary(@NonNull Coordinate coordinate, @NonNull Destination destination, @NonNull RouteSummaryResponseListener routeSummaryResponseListener, @NonNull RouteSummaryOptions routeSummaryOptions) {

    }

    @Override
    public boolean hasPendingRequests() {
        return false;
    }

    @Override
    public void cancelRouteRequests() {}
}
