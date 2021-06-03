package com.github.onedirection.navigation.map;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.navigation.fragment.map.MarkerSymbolManager;
import com.github.onedirection.navigation.fragment.map.MyLocationSymbolManager;
import com.github.onedirection.navigation.fragment.map.NavigationManager;
import com.github.onedirection.testhelpers.WaitAction;
import com.github.onedirection.utils.Id;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapquest.navigation.listener.NavigationStateListener;
import com.mapquest.navigation.model.RouteStoppedReason;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZonedDateTime;
import java.util.concurrent.Semaphore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)

public class MapFragmentTest extends MapFragmentTestSetup {

    private final LatLng TEST_VALUE_LATLNG_1 = new LatLng(40.6974034,-74.1197629);
    private final LatLng TEST_VALUE_LATLNG_2 = new LatLng(34.0201613,-118.6919115);


    private final Event[] testEvents = new Event[] {
            new Event(Id.generateRandom(), "Event 1 New York", "New York USA",
                    new Coordinates(TEST_VALUE_LATLNG_1.getLatitude(), TEST_VALUE_LATLNG_1.getLongitude()),
                    ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5),false),
            new Event(Id.generateRandom(), "Event 2 Los Angeles", "Los Angeles",
                    new Coordinates(TEST_VALUE_LATLNG_2.getLatitude(), TEST_VALUE_LATLNG_2.getLongitude()),
                    ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5),false),
    };

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
    public void findRouteBetweenEventsWorks() throws InterruptedException {
        IdlingRegistry.getInstance().register(fragment.waitForRoute);

        Event start = testEvents[0];
        Event end = testEvents[1];
        Database.getDefaultInstance().store(start);
        Database.getDefaultInstance().store(end);

        BottomSheetBehavior<View> bsb = getFragmentField("bottomSheetBehavior", BottomSheetBehavior.class);
        getFragmentField("markerSymbolManager", MarkerSymbolManager.class).syncEventsWithDb();

        focusEventOnMap(start);
        onView(withId(R.id.mapView))
                .perform(new WaitAction(10000))
                .perform(click())
                .perform(new WaitAction(1000));

        Semaphore semaphore = new Semaphore(0);
        getFragmentField("bottomSheetBehavior", BottomSheetBehavior.class).addBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    semaphore.release();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        runOnUiThreadAndWaitEndExecution(() -> bsb.setState(BottomSheetBehavior.STATE_EXPANDED));
        semaphore.acquire();

        onView(withId(R.id.fragment_map_ui))
                .perform(new WaitAction(1000));
        onView(withId(R.id.fragment_map_event_nav_route_button))
                .perform(click());

        focusEventOnMap(end);
        onView(withId(R.id.mapView))
                .perform(click())
                .perform(new WaitAction(1000));

        runOnUiThreadAndWaitEndExecution(() -> bsb.setState(BottomSheetBehavior.STATE_EXPANDED));
        semaphore.acquire();

        onView(withId(R.id.fragment_map_ui))
                .perform(new WaitAction(1000));
        onView(withId(R.id.fragment_map_event_nav_route_button))
                .perform(click());
        onView(withId(R.id.fragment_map_ui))
                .perform(new WaitAction(1000));

        // the idling resource ensures it works, otherwise it timeouts after ~30 secs
    }

    @Test
    public void startNavWithUiWorks() throws InterruptedException {
        IdlingRegistry.getInstance().register(fragment.waitForNavStart);
        Event dest = testEvents[0];
        Database.getDefaultInstance().store(dest);

        BottomSheetBehavior<View> bsb = getFragmentField("bottomSheetBehavior", BottomSheetBehavior.class);

        Semaphore semaphore = new Semaphore(0);
        bsb.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    semaphore.release();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        focusEventOnMap(dest);
        onView(withId(R.id.mapView))
                .perform(new WaitAction(1000))
                .perform(click())
                .perform(new WaitAction(1000));

        runOnUiThreadAndWaitEndExecution(() -> bsb.setState(BottomSheetBehavior.STATE_EXPANDED));
        semaphore.acquire();

        onView(withId(R.id.fragment_map_ui))
                .perform(new WaitAction(1000));

        NavigationManager navigationManager = getFragmentField("navigationManager", NavigationManager.class);
        com.mapquest.navigation.NavigationManager navigationManager1 = getAttributeField("navigationManager",
                navigationManager, com.mapquest.navigation.NavigationManager.class);
        navigationManager1.addNavigationStateListener(new NavigationStateListener() {
            @Override
            public void onNavigationStarted() {
                semaphore.release();
            }

            @Override
            public void onNavigationStopped(@NonNull RouteStoppedReason routeStoppedReason) {}

            @Override
            public void onNavigationPaused() {}

            @Override
            public void onNavigationResumed() {}
        });

        onView(withId(R.id.fragment_map_ui))
                .perform(new WaitAction(2000));

        onView(withId(R.id.fragment_map_event_nav_button))
                .perform(click());
        semaphore.acquire();
        
        assertThat(navigationManager1.getNavigationState(), equalTo(com.mapquest.navigation.NavigationManager
                .NavigationState.ACTIVE));
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

}
