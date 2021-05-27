package com.github.onedirection.map;

import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.onedirection.BuildConfig;
import com.github.onedirection.R;
import com.github.onedirection.database.implementation.ConcreteDatabase;
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.implementation.DefaultDatabase;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.geolocation.model.NamedCoordinates;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.navigation.fragment.map.DeviceLocationProviderAdapter;
import com.github.onedirection.navigation.fragment.map.MapFragment;
import com.github.onedirection.navigation.fragment.map.MarkerSymbolManager;
import com.github.onedirection.navigation.fragment.map.MyLocationSymbolManager;
import com.github.onedirection.navigation.fragment.map.NavigationManager;
import com.github.onedirection.navigation.fragment.map.RouteDisplayManager;
import com.github.onedirection.navigation.fragment.map.RoutesManager;
import com.github.onedirection.testhelpers.WaitAction;
import com.github.onedirection.utils.EspressoIdlingResource;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.Pair;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.listener.NavigationProgressListener;
import com.mapquest.navigation.listener.NavigationStateListener;
import com.mapquest.navigation.listener.RerouteBehaviorOverride;
import com.mapquest.navigation.model.Maneuver;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteLeg;
import com.mapquest.navigation.model.RouteStoppedReason;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Destination;
import com.mapquest.navigation.model.location.Location;
import com.mapquest.navigation.model.location.LocationObservation;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MapFragmentTest {

    private MapboxMap mapboxMap;
    private MapFragment fragment;
    private OnMapReadyIdlingResource onMapReadyIdlingResource;
    private EspressoIdlingResource espressoIdlingResource;
    private CountingIdlingResource countingIdlingResource;

    private final LatLng TEST_VALUE_LATLNG_1 = new LatLng(2f, 0.003f);
    private final LatLng TEST_VALUE_LATLNG_2 = new LatLng(34f, 0.1543f);
    private final LatLng TEST_VALUE_LATLNG_3 = new LatLng(40.6974034,-74.1197629);
    private final LatLng TEST_VALUE_LATLNG_4 = new LatLng(42.355097, -71.055464);
    private final LatLng TEST_VALUE_LATLNG_5 = new LatLng(34.0, -50.0);
    private final LatLng TEST_VALUE_LATLNG_6 = new LatLng(34.0201613,-118.6919115);
    //private final Event TEST_EVENT_1 = new Event(Id.generateRandom(), "Test event", "Paris",
    private final Event TEST_EVENT_1 = new Event(Id.generateRandom(), "Test event", new NamedCoordinates(48.511197, 2.205589, "Paris"),
            ZonedDateTime.of(2021, 4, 2, 13, 42, 56, 0, ZoneId.systemDefault()),
            ZonedDateTime.of(2021, 4, 2, 13, 58, 56, 0, ZoneId.systemDefault()), false);

    public static final double LOCATION_1_latitude = 32.22222;
    public static final double LOCATION_1_longitude = 43.33333;
    public static final Coordinates COORDINATES_LOCATION = new Coordinates(32.22222, 43.33333);
    public static final LatLng LAT_LNG_1 = new LatLng(40.7326808, -73.9843407);
    public static final LatLng LAT_LNG_2 = new LatLng(40.7326808, -73.9843407);

    private final Event[] testEvents = new Event[] {
            new Event(Id.generateRandom(), "Event 1 Paris", "Paris France", new Coordinates(TEST_VALUE_LATLNG_1.getLatitude(), TEST_VALUE_LATLNG_1.getLongitude()), ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5), false),
            new Event(Id.generateRandom(), "Event 2 Moscow", "Moscow Russia", new Coordinates(TEST_VALUE_LATLNG_2.getLatitude(), TEST_VALUE_LATLNG_2.getLongitude()), ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5), false),
            new Event(Id.generateRandom(), "Event 3 New York", "New York USA", new Coordinates(TEST_VALUE_LATLNG_3.getLatitude(), TEST_VALUE_LATLNG_3.getLongitude()), ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5), false),
            new Event(Id.generateRandom(), "Event 4 Lagos", "Lagos Nigeria", new Coordinates(TEST_VALUE_LATLNG_4.getLatitude(), TEST_VALUE_LATLNG_4.getLongitude()), ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5), false),
            new Event(Id.generateRandom(), "Event 5 Santiago", "Santiago Chile", new Coordinates(TEST_VALUE_LATLNG_5.getLatitude(), TEST_VALUE_LATLNG_5.getLongitude()), ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5), false),
            new Event(Id.generateRandom(), "Event 6 Los Angeles", "Los Angeles", new Coordinates(TEST_VALUE_LATLNG_6.getLatitude(), TEST_VALUE_LATLNG_6.getLongitude()), ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5), false),
    };

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(
                    Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void setupForTest() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_map)).perform(ViewActions.click());

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_map)));
        testRule.getScenario().onActivity(activity -> {
            fragment = (MapFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment)
                    .getChildFragmentManager().getPrimaryNavigationFragment();
            onMapReadyIdlingResource = new OnMapReadyIdlingResource(fragment);
        });

        espressoIdlingResource = EspressoIdlingResource.getInstance();
        countingIdlingResource = espressoIdlingResource.getCountingIdlingResource();

        IdlingRegistry.getInstance().register(onMapReadyIdlingResource);
        IdlingRegistry.getInstance().register(countingIdlingResource);
        onView(withId(R.id.mapView)).check(matches(isDisplayed()));
        mapboxMap = onMapReadyIdlingResource.getMapboxMap();
    }

    @Before
    public void deleteAllEvents() throws ExecutionException, InterruptedException {
        ConcreteDatabase db = DefaultDatabase.getDefaultConcreteInstance();
        List<Event> events = db.retrieveAll(EventStorer.getInstance()).get();
        for(Event e : events) {
            Id id = db.remove(e.getId(), EventStorer.getInstance()).get();
            assertEquals(e.getId(), id);
        }
    }

    @After
    public void AtEndTest() {
        IdlingRegistry.getInstance().unregister(onMapReadyIdlingResource);
        IdlingRegistry.getInstance().unregister(countingIdlingResource);
    }

    @Test
    public void isOnMapReadyIdlingResourceWorking() {
        assertThat(mapboxMap, is(notNullValue()));
    }

    @Test
    public void isIdlingResourceWorkingForManagersInitializing() {
        assertThat(getFragmentField("markerSymbolManager", MarkerSymbolManager.class), is(notNullValue()));
        assertThat(getFragmentField("myLocationSymbolManager", MyLocationSymbolManager.class), is(notNullValue()));
    }

    @Test
    public void testMarkerSymbolManager() throws InterruptedException {
        MarkerSymbolManager markerSymbolManager = getFragmentField("markerSymbolManager", MarkerSymbolManager.class);

        final Symbol[] marker = new Symbol[1];
        runOnUiThreadAndWaitEndExecution(() -> marker[0] = markerSymbolManager.addMarker(TEST_VALUE_LATLNG_1));

        List<Symbol> markers = markerSymbolManager.getAllMarkers();
        assertThat(markers.size(), is(1));
        assertThat(markers.get(0).getLatLng(), is(TEST_VALUE_LATLNG_1));

        runOnUiThreadAndWaitEndExecution(() -> markerSymbolManager.removeMarker(marker[0]));

        markers = markerSymbolManager.getAllMarkers();
        assertThat(markers.size(), is(0));

        runOnUiThreadAndWaitEndExecution(() -> marker[0] = markerSymbolManager.addMarker(TEST_VALUE_LATLNG_2));
        runOnUiThreadAndWaitEndExecution(markerSymbolManager::removeAllMarkers);

        assertThat(markers.size(), is(0));
    }

    @Test
    public void testAddEventPutsMarkerOnMap() throws InterruptedException {
        // Wait acton to make getMarkerSymbolManager work.
        MarkerSymbolManager markerSymbolManager = getFragmentField("markerSymbolManager", MarkerSymbolManager.class);
        Pair<Symbol, LatLng>[] pair = new Pair[1];
        runOnUiThreadAndWaitEndExecution(() -> pair[0] = markerSymbolManager.addGeocodedEventMarker(TEST_EVENT_1).join());

        BottomSheetBehavior<View> bsb = getFragmentField("bottomSheetBehavior", BottomSheetBehavior.class);
        assertThat(bsb.getState(), is(BottomSheetBehavior.STATE_HIDDEN));

        Semaphore semaphore = new Semaphore(0);

        runOnUiThreadAndWaitEndExecution(() -> {
            // need to zoom to center the marker and make the next click() click it
            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                    .target(pair[0].second)
                    .zoom(15.)
                    .build());
        });

        getFragmentField("bottomSheetBehavior", BottomSheetBehavior.class).addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    semaphore.release();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        onView(withId(R.id.mapView)).perform(new WaitAction(3000)).perform(click());
        semaphore.acquire();

        assertThat(bsb.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
        onView(withId(R.id.fragment_map_event_name)).check(matches(withText(TEST_EVENT_1.getName())));
    }

    @Test
    public void testDbEventsAppearOnMap() throws InterruptedException {
        Database db = Database.getDefaultInstance();
        db.storeAll(Arrays.asList(testEvents)).join();

        // Wait acton to make getMarkerSymbolManager work.
        onView(withId(R.id.mapView)).perform(new WaitAction(1000));
        MarkerSymbolManager markerSymbolManager = getFragmentField("markerSymbolManager", MarkerSymbolManager.class);;
        markerSymbolManager.syncEventsWithDb().join();

        Semaphore waitForBsbCollapsed = new Semaphore(0);
        Semaphore waitForBsbHidden = new Semaphore(0);

        BottomSheetBehavior<View> bsb = getFragmentField("bottomSheetBehavior", BottomSheetBehavior.class);
        bsb.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    Log.d("MapFragmentTest", "bsb state collapsed.");
                    waitForBsbCollapsed.release();
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    Log.d("MapFragmentTest", "bsb state hidden.");
                    waitForBsbHidden.release();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        });

        for (Event e : testEvents) {
            Log.d("MapFragmentTest", "e: " + e);
            runOnUiThreadAndWaitEndExecution(() -> {
                // need to zoom to center the marker and make the next click() click it
                Log.d("MapFragmentTest", "about to release.");
                mapboxMap.setCameraPosition(new CameraPosition.Builder()
                        .target(e.getCoordinates().get().toLatLng())
                        .zoom(15.)
                        .build()
                );
            });
            onView(withId(R.id.mapView)).perform(new WaitAction(3000)).perform(click());
            waitForBsbCollapsed.acquire(); // wait for the bsb to settle

            assertThat(bsb.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
            Log.d("MapFragmentTest", "map_event_name: " + fragment.getActivity().findViewById(R.id.fragment_map_event_name));

            onView(withId(R.id.fragment_map_event_name)).check(matches(withText(e.getName())));

            runOnUiThreadAndWaitEndExecution(() ->
                    bsb.setState(BottomSheetBehavior.STATE_HIDDEN)
            );
            waitForBsbHidden.acquire();
        }
    }

    @Test
    public void testMyLocationIsAppearing() throws InterruptedException {
        MyLocationSymbolManager myLocationSymbolManager = getFragmentField("myLocationSymbolManager", MyLocationSymbolManager.class);
        DeviceLocationProviderMock deviceLocationProviderMock = new DeviceLocationProviderMock();
        deviceLocationProviderMock.addObserver((subject, value) -> {
            if (myLocationSymbolManager != null) {
                try {
                    runOnUiThreadAndWaitEndExecution(() -> myLocationSymbolManager.update(value));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        deviceLocationProviderMock.notifyObservers();
        setFragmentField("deviceLocationProvider", deviceLocationProviderMock);
        LatLng last = mapboxMap.getCameraPosition().target;
        assertThat(myLocationSymbolManager.getPosition(), is(notNullValue()));
        Semaphore semaphore = new Semaphore(0);
        onView(withId(R.id.my_location_button)).perform(click());
        mapboxMap.addOnCameraIdleListener(new MapboxMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                semaphore.release();
            }
        });
        semaphore.acquire();
        LatLng next = mapboxMap.getCameraPosition().target;
        assertThat(next.equals(last), is(false));
    }

    @Test
    public void testMyLocationButton() {
        MyLocationSymbolManager myLocationSymbolManager = getFragmentField("myLocationSymbolManager", MyLocationSymbolManager.class);
        DeviceLocationProviderMock deviceLocationProviderMock = new DeviceLocationProviderMock();
        deviceLocationProviderMock.addObserver((subject, value) -> {
            if (myLocationSymbolManager != null) {
                try {
                    runOnUiThreadAndWaitEndExecution(() -> myLocationSymbolManager.update(value));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        deviceLocationProviderMock.notifyObservers();
        setFragmentField("deviceLocationProvider", deviceLocationProviderMock);
        onView(withId(R.id.my_location_button)).perform(click());
        assertThat(myLocationSymbolManager.getPosition(), is(notNullValue()));
    }

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
            routesManager.findRoute(TEST_VALUE_LATLNG_3, Collections.singletonList(TEST_VALUE_LATLNG_4), new RoutesResponseListener() {
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

    private void runOnUiThreadAndWaitEndExecution(Runnable runnable) throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        fragment.requireActivity().runOnUiThread(() -> {
            runnable.run();
            semaphore.release();
        });
        semaphore.acquire();
    }

    @Test
    public void testNavigation() throws InterruptedException {
        RoutesManager routesManager = getFragmentField("routesManager", RoutesManager.class);
        RouteDisplayManager routeDisplayManager = getFragmentField("routeDisplayManager", RouteDisplayManager.class);
        NavigationManager navigationManager = getFragmentField("navigationManager", NavigationManager.class);
        final com.mapquest.navigation.NavigationManager[] nav = new com.mapquest.navigation.NavigationManager[1];
        runOnUiThreadAndWaitEndExecution(() -> nav[0] = new com.mapquest.navigation.NavigationManager.Builder(
                fragment.requireContext().getApplicationContext(), BuildConfig.API_KEY,
                new DeviceLocationProviderAdapter(new DeviceLocationProviderMock()))
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
        runOnUiThreadAndWaitEndExecution(() -> routesManager.findRoute(TEST_VALUE_LATLNG_3, Collections.singletonList(TEST_VALUE_LATLNG_4), new RoutesResponseListener() {
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
        DeviceLocationProviderMock deviceLocationProviderMock = new DeviceLocationProviderMock();
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
        runOnUiThreadAndWaitEndExecution(() -> routesManager.findRoute(TEST_VALUE_LATLNG_3, Collections.singletonList(TEST_VALUE_LATLNG_4), new RoutesResponseListener() {
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

    @Test
    public void findRouteBetweenEventsWorks() throws InterruptedException {
        IdlingRegistry.getInstance().register(fragment.waitForRoute);

        Event start = testEvents[2];
        Event end = testEvents[5];
        Database.getDefaultInstance().store(start);
        Database.getDefaultInstance().store(end);

        BottomSheetBehavior<View> bsb = getFragmentField("bottomSheetBehavior", BottomSheetBehavior.class);

        focusEventOnMap(start);
        onView(withId(R.id.mapView))
                .perform(new WaitAction(1000))
                .perform(click())
                .perform(new WaitAction(1000));

        runOnUiThreadAndWaitEndExecution(() -> bsb.setState(BottomSheetBehavior.STATE_EXPANDED));

        onView(withId(R.id.fragment_map_ui))
                .perform(new WaitAction(1000));
        onView(withId(R.id.fragment_map_event_nav_route_button))
                .perform(click());

        focusEventOnMap(end);
        onView(withId(R.id.mapView))
                .perform(click())
                .perform(new WaitAction(1000));

        runOnUiThreadAndWaitEndExecution(() -> bsb.setState(BottomSheetBehavior.STATE_EXPANDED));

        onView(withId(R.id.fragment_map_ui))
                .perform(new WaitAction(1000));
        onView(withId(R.id.fragment_map_event_nav_route_button))
                .perform(click());
        onView(withId(R.id.fragment_map_ui))
                .perform(new WaitAction(1000));

        // the idling resource ensures it works, otherwise it timeouts after ~30 secs
    }

    @Ignore("Cirrus' reject")
    @Test
    public void startNavWithUiWorks() throws InterruptedException {
        IdlingRegistry.getInstance().register(fragment.waitForNavStart);
        Event dest = testEvents[2];
        Database.getDefaultInstance().store(dest);

        BottomSheetBehavior<View> bsb = getFragmentField("bottomSheetBehavior", BottomSheetBehavior.class);

        focusEventOnMap(dest);
        onView(withId(R.id.mapView))
                .perform(new WaitAction(1000))
                .perform(click())
                .perform(new WaitAction(1000));

        runOnUiThreadAndWaitEndExecution(() -> bsb.setState(BottomSheetBehavior.STATE_EXPANDED));

        onView(withId(R.id.fragment_map_ui))
                .perform(new WaitAction(1000));

        onView(withId(R.id.fragment_map_event_nav_button))
                .perform(click());

        NavigationManager navigationManager = getFragmentField("navigationManager", NavigationManager.class);
        com.mapquest.navigation.NavigationManager navigationManager1 = getAttributeField("navigationManager", navigationManager, com.mapquest.navigation.NavigationManager.class);
        assertThat(navigationManager1.getNavigationState(), equalTo(com.mapquest.navigation.NavigationManager.NavigationState.ACTIVE));
    }

    private void focusEventOnMap(Event e) throws InterruptedException {
        runOnUiThreadAndWaitEndExecution(() -> {
            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                    .target(e.getCoordinates().get().toLatLng())
                    .zoom(15.)
                    .build()
            );
        });
    }

    private <T> T getFragmentField(String fieldName, Class<T> classToCast) {
        return getAttributeField(fieldName, fragment, classToCast);
    }

    private <T, S> S getAttributeField(String fieldName, T attribute, Class<S> classToCast) {
        try {
            Field field = attribute.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return ((S) field.get(attribute));
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    private <T> void setFragmentField(String fieldName, T value) {
        setAttributeField(fieldName, fragment, value);
    }

    private <T, S> void setAttributeField(String fieldName, T attribute, S value) {
        try {
            Field field = attribute.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(attribute, value);
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }
}
