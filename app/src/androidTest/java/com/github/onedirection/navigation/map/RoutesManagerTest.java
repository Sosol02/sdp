package com.github.onedirection.navigation.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.navigation.fragment.map.RouteDisplayManager;
import com.github.onedirection.navigation.fragment.map.RoutesManager;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.model.Route;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AndroidJUnit4.class)
public class RoutesManagerTest extends MapFragmentTestSetup {

    private final LatLng TEST_VALUE_LATLNG_1 = new LatLng(40.6974034,-74.1197629);
    private final LatLng TEST_VALUE_LATLNG_2 = new LatLng(42.355097, -71.055464);

    @Test
    public void testRoutesManagerInit() {
        RoutesManager routesManager = getFragmentField("routesManager", RoutesManager.class);
        RouteDisplayManager routeDisplayManager = getFragmentField("routeDisplayManager", RouteDisplayManager.class);
        List<Line> lines = getAttributeField("lines", routeDisplayManager, List.class);
        List<Route> routes = getAttributeField("routes", routesManager, List.class);

        assertThat(lines, is(nullValue()));
        assertThat(routes, is(nullValue()));
    }

    @Test
    public void testRoutesManagerFindMethod() throws InterruptedException {
        RoutesManager routesManager = getFragmentField("routesManager", RoutesManager.class);
        RouteDisplayManager routeDisplayManager = getFragmentField("routeDisplayManager", RouteDisplayManager.class);
        RouteService routeService = new RouteServiceMock();
        setAttributeField("routeService", routesManager, routeService);
        Semaphore semaphore = new Semaphore(0);
        runOnUiThreadAndWaitEndExecution(() -> {
            routesManager.findRoute(TEST_VALUE_LATLNG_1, Collections.singletonList(TEST_VALUE_LATLNG_2), new RoutesResponseListener() {
                @Override
                public void onRoutesRetrieved(@NonNull List<Route> list) {
                    routeDisplayManager.displayRoute(list.get(0));
                    semaphore.release();
                }

                @Override
                public void onRequestFailed(@Nullable Integer integer, @Nullable IOException e) {}

                @Override
                public void onRequestMade() {}
            });
        });
        try {
            semaphore.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Line> lines = getAttributeField("lines", routeDisplayManager, List.class);
        List<Route> routes = getAttributeField("routes", routesManager, List.class);

        assertThat(routes, is(notNullValue()));
        assertThat(lines, is(notNullValue()));

        runOnUiThreadAndWaitEndExecution(routesManager::clearRoutes);
        runOnUiThreadAndWaitEndExecution(routeDisplayManager::clearDisplayedRoute);

        lines = getAttributeField("lines", routeDisplayManager, List.class);
        routes = getAttributeField("routes", routesManager, List.class);

        assertThat(lines, is(nullValue()));
        assertThat(routes, is(nullValue()));
    }
}
