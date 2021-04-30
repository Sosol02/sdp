package com.github.onedirection.map;

import android.Manifest;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.onedirection.R;
import com.github.onedirection.events.Event;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.navigation.fragment.map.MapFragment;
import com.github.onedirection.navigation.fragment.map.MarkerSymbolManager;
import com.github.onedirection.navigation.fragment.map.MyLocationSymbolManager;
import com.github.onedirection.navigation.fragment.map.RoutesManager;
import com.github.onedirection.testhelpers.WaitAction;
import com.github.onedirection.utils.EspressoIdlingResource;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.Pair;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapquest.navigation.model.Route;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AndroidJUnit4.class)
public class MapFragmentTest {

    private MapboxMap mapboxMap;
    private MapFragment fragment;
    private OnMapReadyIdlingResource onMapReadyIdlingResource;
    private EspressoIdlingResource espressoIdlingResource;

    private final LatLng TEST_VALUE_LATLNG_1 = new LatLng(2f, 0.003f);
    private final LatLng TEST_VALUE_LATLNG_2 = new LatLng(34f, 0.1543f);
    private final LatLng TEST_VALUE_LATLNG_3 = new LatLng(40.7326808, -73.9843407);
    private final LatLng TEST_VALUE_LATLNG_4 = new LatLng(42.355097, -71.055464);
    private final Event TEST_EVENT_1 = new Event(Id.generateRandom(), "Test event", "Paris",
            ZonedDateTime.of(2021, 4, 2, 13, 42, 56, 0, ZoneId.systemDefault()),
            ZonedDateTime.of(2021, 4, 2, 13, 58, 56, 0, ZoneId.systemDefault()));

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
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

        IdlingRegistry.getInstance().register(onMapReadyIdlingResource);
        IdlingRegistry.getInstance().register(espressoIdlingResource.getCountingIdlingResource());
        onView(withId(R.id.mapView)).check(matches(isDisplayed()));
        mapboxMap = onMapReadyIdlingResource.getMapboxMap();
    }

    @After
    public void AtEndTest() {
        IdlingRegistry.getInstance().unregister(onMapReadyIdlingResource);
        IdlingRegistry.getInstance().unregister(espressoIdlingResource.getCountingIdlingResource());
    }

    @Test
    public void isOnMapReadyIdlingResourceWorking() {
        assertThat(mapboxMap, is(notNullValue()));
        assertThat(getMarkerSymbolManager(), is(notNullValue()));
    }

    @Test
    public void isIdlingResourceWorkingForManagersInitializing() {
        assertThat(getMarkerSymbolManager(), is(notNullValue()));
        assertThat(getMyLocationSymbolManager(), is(notNullValue()));
    }

    @Test
    public void testMarkerSymbolManager() {
        MarkerSymbolManager markerSymbolManager = getMarkerSymbolManager();

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
    public void testAddEventPutsMarkerOnMap() {
        // Wait acton to make getMarkerSymbolManager work.
        onView(withId(R.id.mapView)).perform(new WaitAction(1000));
        MarkerSymbolManager markerSymbolManager = getMarkerSymbolManager();
        Pair<Symbol, LatLng> pair;
        try {
            pair = markerSymbolManager.addGeocodedEventMarker(TEST_EVENT_1).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BottomSheetBehavior<View> bsb = getBottomSheetBehavior();
        assertThat(bsb.getState(), is(BottomSheetBehavior.STATE_HIDDEN));

        Semaphore semaphore = new Semaphore(0);

        runOnUiThreadAndWaitEndExecution(() -> {
            // need to zoom to center the marker and make the next click() click it
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pair.second, 15));
            mapboxMap.addOnCameraIdleListener(semaphore::release);
        });
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getBottomSheetBehavior().addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
        onView(withId(R.id.mapView)).perform(click());
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(bsb.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
        onView(withId(R.id.fragment_map_event_name)).check(matches(withText(TEST_EVENT_1.getName())));
    }

    @Test
    @Ignore("Cirrus reject")
    public void testMyLocationIsAppearing() {
        MyLocationSymbolManager myLocationSymbolManager = getMyLocationSymbolManager();
        LatLng last = mapboxMap.getCameraPosition().target;
        assertThat(myLocationSymbolManager.getPosition(), is(notNullValue()));
        onView(withId(R.id.my_location_button)).perform(click()).perform(new WaitAction(10000));
        LatLng next = mapboxMap.getCameraPosition().target;
        assertThat(next.equals(last), is(false));
    }

    @Test
    public void testMyLocationButton() {
        MyLocationSymbolManager myLocationSymbolManager = getMyLocationSymbolManager();
        onView(withId(R.id.my_location_button)).perform(click()).perform(new WaitAction(10000));
        assertThat(myLocationSymbolManager.getPosition(), is(nullValue()));
    }

    @Test
    public void testRoutesManagerInit() {
        RoutesManager routesManager = getRoutesManager();
        List<Line> lines = getRoutesManagerLines(routesManager);
        List<Route> routes = getRoutesManagerRoutes(routesManager);

        assertThat(lines, is(nullValue()));
        assertThat(routes, is(nullValue()));

        runOnUiThreadAndWaitEndExecution(() -> {
            routesManager.findRoute(TEST_VALUE_LATLNG_3, TEST_VALUE_LATLNG_4);
        });
    }

    @Test
    @Ignore("Cirrus reject")
    public void testRoutesManagerFindMethod() {
        RoutesManager routesManager = getRoutesManager();
        Semaphore semaphore = new Semaphore(0);
        runOnUiThreadAndWaitEndExecution(() -> {
            routesManager.findRoute(TEST_VALUE_LATLNG_3, TEST_VALUE_LATLNG_4, semaphore::release);
        });
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Line> lines = getRoutesManagerLines(routesManager);
        List<Route> routes = getRoutesManagerRoutes(routesManager);

        assertThat(lines, is(notNullValue()));
        assertThat(routes, is(notNullValue()));

        runOnUiThreadAndWaitEndExecution(routesManager::clearRoutesManager);

        lines = getRoutesManagerLines(routesManager);
        routes = getRoutesManagerRoutes(routesManager);

        assertThat(lines, is(nullValue()));
        assertThat(routes, is(nullValue()));
    }

    private void runOnUiThreadAndWaitEndExecution(BlockingCustomCodeOnRuiUiThread blockingCustomCodeOnRuiUiThread) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                blockingCustomCodeOnRuiUiThread.onRunOnUIThread();
                synchronized (this) {
                    this.notify();
                }
            }
        };
        synchronized (runnable) {
            fragment.requireActivity().runOnUiThread(runnable);
            try {
                runnable.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private MarkerSymbolManager getMarkerSymbolManager() {
        try {
            Field field = fragment.getClass().getDeclaredField("markerSymbolManager");
            field.setAccessible(true);
            return ((MarkerSymbolManager) field.get(fragment));
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    private MyLocationSymbolManager getMyLocationSymbolManager() {
        try {
            Field field = fragment.getClass().getDeclaredField("myLocationSymbolManager");
            field.setAccessible(true);
            return ((MyLocationSymbolManager) field.get(fragment));
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    private RoutesManager getRoutesManager() {
        try {
            Field field = fragment.getClass().getDeclaredField("routesManager");
            field.setAccessible(true);
            return ((RoutesManager) field.get(fragment));
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    private List<Line> getRoutesManagerLines(RoutesManager routesManager) {
        try {
            Field field = routesManager.getClass().getDeclaredField("lines");
            field.setAccessible(true);
            return ((List<Line>) field.get(routesManager));
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    private List<Route> getRoutesManagerRoutes(RoutesManager routesManager) {
        try {
            Field field = routesManager.getClass().getDeclaredField("routes");
            field.setAccessible(true);
            return ((List<Route>) field.get(routesManager));
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    private BottomSheetBehavior<View> getBottomSheetBehavior() {
        try {
            Field field = fragment.getClass().getDeclaredField("bottomSheetBehavior");
            field.setAccessible(true);
            return (BottomSheetBehavior<View>) field.get(fragment);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private interface BlockingCustomCodeOnRuiUiThread {

        void onRunOnUIThread();
    }
}
