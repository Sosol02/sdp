package com.github.onedirection.navigation.fragment.map;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.onedirection.BuildConfig;
import com.github.onedirection.utils.EspressoIdlingResource;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.internal.collection.CollectionsUtil;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteOptionType;
import com.mapquest.navigation.model.RouteOptions;
import com.mapquest.navigation.model.SystemOfMeasurement;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Destination;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * RoutesManager is use with mapquest to able to query a route between events
 */
public class RoutesManager {

    private final RouteService routeService;
    private List<Route> routes;
    private Context context;

    public RoutesManager(Context context) {
        Objects.requireNonNull(context);
        this.routeService = new RouteService.Builder().build(context,
                BuildConfig.API_KEY);
        this.context = context;
    }

    public void findRoute(LatLng start, List<LatLng> destinations, RoutesResponseListener routesResponseListener) {
        clearRoutes();
        Coordinate from = new Coordinate(start.getLatitude(), start.getLongitude());
        List<Destination> to = new ArrayList<>();
        for (LatLng destination : destinations) {
            Coordinate tmp = new Coordinate(destination.getLatitude(), destination.getLongitude());
            to.add(new Destination(tmp, null));
        }

        RouteOptions routeOptions = new RouteOptions.Builder()
                .maxRoutes(3)
                .systemOfMeasurementForDisplayText(SystemOfMeasurement.METRIC)
                .language(Locale.getDefault().toLanguageTag().replace("-", "_"))
                .highways(RouteOptionType.ALLOW)
                .tolls(RouteOptionType.ALLOW)
                .ferries(RouteOptionType.DISALLOW)
                .internationalBorders(RouteOptionType.ALLOW)
                .unpaved(RouteOptionType.DISALLOW)
                .seasonalClosures(RouteOptionType.DISALLOW)
                .build();

        EspressoIdlingResource.getInstance().lockIdlingResource();
        routeService.requestRoutes(from, to, routeOptions, new RoutesResponseListener() {
            @Override
            public void onRoutesRetrieved(List<Route> routes1) {
                if (routes1.size() > 0) {
                    Toast.makeText(context, "Route has been retrevied", Toast.LENGTH_LONG).show();
                    routes = routes1;
                }
                routesResponseListener.onRoutesRetrieved(routes1);
                EspressoIdlingResource.getInstance().unlockIdlingResource();
            }

            @Override
            public void onRequestFailed(@Nullable Integer httpStatusCode, @Nullable IOException exception) {
                Toast.makeText(context, "Route request has failed", Toast.LENGTH_LONG).show();
                routesResponseListener.onRequestFailed(httpStatusCode, exception);
                EspressoIdlingResource.getInstance().unlockIdlingResource();
            }

            @Override
            public void onRequestMade() {
                Toast.makeText(context, "Route has been requested", Toast.LENGTH_LONG).show();
                routesResponseListener.onRequestMade();
            }
        });
    }

    public long getTimeUntilArrival(@NonNull Route route) {
        return CollectionsUtil.lastValue(route.getLegs()).getTraffic().getEstimatedTimeOfArrival().getTime();
    }

    public void clearRoutes() {
        routes = null;
    }
}
