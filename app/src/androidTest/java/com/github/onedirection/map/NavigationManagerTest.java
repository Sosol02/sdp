package com.github.onedirection.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.BuildConfig;
import com.github.onedirection.R;
import com.github.onedirection.navigation.fragment.map.DeviceLocationProviderAdapter;
import com.github.onedirection.navigation.fragment.map.NavigationManager;
import com.github.onedirection.navigation.fragment.map.RouteDisplayManager;
import com.github.onedirection.navigation.fragment.map.RoutesManager;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.listener.NavigationStateListener;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteStoppedReason;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class NavigationManagerTest extends MapFragmentTestSetup {

    private final LatLng TEST_VALUE_LATLNG_1 = new LatLng(40.6974034,-74.1197629);
    private final LatLng TEST_VALUE_LATLNG_2 = new LatLng(42.355097, -71.055464);

    @Test
    public void testNavigation() throws InterruptedException {
        RoutesManager routesManager = getFragmentField("routesManager", RoutesManager.class);
        RouteDisplayManager routeDisplayManager = getFragmentField("routeDisplayManager", RouteDisplayManager.class);
        NavigationManager navigationManager = getFragmentField("navigationManager", NavigationManager.class);
        final com.mapquest.navigation.NavigationManager[] nav = new com.mapquest.navigation.NavigationManager[1];
        runOnUiThreadAndWaitEndExecution(() -> nav[0] = new com.mapquest.navigation.NavigationManager.Builder(
                fragment.requireContext().getApplicationContext(), BuildConfig.API_KEY,
                new DeviceLocationProviderAdapter(new DeviceLocationProviderMock(true)))
                .build());
        setAttributeField("navigationManager", navigationManager, nav[0]);

        final boolean[] isNavigationStarted = {false};

        com.mapquest.navigation.NavigationManager navigationManager1 = getAttributeField("navigationManager", navigationManager, com.mapquest.navigation.NavigationManager.class);
        navigationManager1.addNavigationStateListener(new NavigationStateListener() {
            @Override
            public void onNavigationStarted() {
                isNavigationStarted[0] = true;
            }

            @Override
            public void onNavigationStopped(@NonNull RouteStoppedReason routeStoppedReason) {
                isNavigationStarted[0] = false;
            }

            @Override
            public void onNavigationPaused() {

            }

            @Override
            public void onNavigationResumed() {

            }
        });

        RouteService routeService = new RouteServiceMock();
        setAttributeField("routeService", routesManager, routeService);
        Semaphore semaphore = new Semaphore(0);
        runOnUiThreadAndWaitEndExecution(() -> routesManager.findRoute(TEST_VALUE_LATLNG_1, Collections.singletonList(TEST_VALUE_LATLNG_2), new RoutesResponseListener() {
            @Override
            public void onRoutesRetrieved(@NonNull List<Route> list) {
                navigationManager.startNavigation(list.get(0));
                semaphore.release();
            }

            @Override
            public void onRequestFailed(@Nullable Integer integer, @Nullable IOException e) {

            }

            @Override
            public void onRequestMade() {

            }
        }));
        try {
            semaphore.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(isNavigationStarted[0], is(true));

        runOnUiThreadAndWaitEndExecution(navigationManager::stopNavigation);
        assertThat(isNavigationStarted[0], is(false));
    }

    @Test
    public void testNavigationUi() throws InterruptedException {
        RoutesManager routesManager = getFragmentField("routesManager", RoutesManager.class);
        RouteDisplayManager routeDisplayManager = getFragmentField("routeDisplayManager", RouteDisplayManager.class);
        NavigationManager navigationManager = getFragmentField("navigationManager", NavigationManager.class);
        final com.mapquest.navigation.NavigationManager[] nav = new com.mapquest.navigation.NavigationManager[1];
        DeviceLocationProviderMock deviceLocationProviderMock = new DeviceLocationProviderMock(true);
        runOnUiThreadAndWaitEndExecution(() -> nav[0] = new com.mapquest.navigation.NavigationManager.Builder(
                fragment.requireContext().getApplicationContext(), BuildConfig.API_KEY,
                new DeviceLocationProviderAdapter(deviceLocationProviderMock))
                .build());
        setAttributeField("navigationManager", navigationManager, nav[0]);
        com.mapquest.navigation.NavigationManager navigationManager1 = getAttributeField("navigationManager", navigationManager, com.mapquest.navigation.NavigationManager.class);
        navigationManager1.setRerouteBehaviorOverride(coordinate -> false);
        RouteService routeService = new RouteServiceMock();
        setAttributeField("routeService", routesManager, routeService);
        Semaphore semaphore = new Semaphore(0);
        runOnUiThreadAndWaitEndExecution(() -> routesManager.findRoute(TEST_VALUE_LATLNG_1, Collections.singletonList(TEST_VALUE_LATLNG_2), new RoutesResponseListener() {
            @Override
            public void onRoutesRetrieved(@NonNull List<Route> list) {
                navigationManager.startNavigation(list.get(0));
                semaphore.release();
            }

            @Override
            public void onRequestFailed(@Nullable Integer integer, @Nullable IOException e) {

            }

            @Override
            public void onRequestMade() {

            }
        }));
        try {
            semaphore.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        onView(withId(R.id.next_maneuver)).check(matches(not(withText(""))));
        onView(withId(R.id.eta_final_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.eta_next_destination)).check(matches(not(withText(""))));
        onView(withId(R.id.arrivalBarLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.maneuverBarLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.destinationReachedBarLayout)).check(matches(not(isDisplayed())));
        onView(withId(R.id.speed_limit_blank)).check(matches(isDisplayed()));
        onView(withId(R.id.speed_limit)).check(matches(withText("40")));

        onView(withId(R.id.stop)).perform(click());

        assertThat(navigationManager1.getNavigationState(), equalTo(com.mapquest.navigation.NavigationManager.NavigationState.STOPPED));
    }
}
