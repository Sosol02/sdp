package com.github.onedirection.navigation.map;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.geolocation.model.NamedCoordinates;
import com.github.onedirection.navigation.fragment.map.MarkerSymbolManager;
import com.github.onedirection.testhelpers.WaitAction;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.Pair;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MarkerSymbolManagerTest extends MapFragmentTestSetup {

    private final LatLng TEST_VALUE_LATLNG_1 = new LatLng(2f, 0.003f);
    private final LatLng TEST_VALUE_LATLNG_2 = new LatLng(34f, 0.1543f);
    private final LatLng TEST_VALUE_LATLNG_3 = new LatLng(40.6974034,-74.1197629);
    private final LatLng TEST_VALUE_LATLNG_4 = new LatLng(42.355097, -71.055464);
    private final LatLng TEST_VALUE_LATLNG_5 = new LatLng(34.0, -50.0);
    private final LatLng TEST_VALUE_LATLNG_6 = new LatLng(34.0201613,-118.6919115);

    private final Event TEST_EVENT_1 = new Event(Id.generateRandom(), "Test event",
            new NamedCoordinates(48.511197, 2.205589, "Paris"),
            ZonedDateTime.of(2021, 4, 2, 13, 42, 56, 0,
                    ZoneId.systemDefault()),
            ZonedDateTime.of(2021, 4, 2, 13, 58, 56, 0,
                    ZoneId.systemDefault()),false);

    private final Event[] testEvents = new Event[] {
            new Event(Id.generateRandom(), "Event 1 Paris", "Paris France",
                    new Coordinates(TEST_VALUE_LATLNG_1.getLatitude(), TEST_VALUE_LATLNG_1.getLongitude()),
                    ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5),false),
            new Event(Id.generateRandom(), "Event 2 Moscow", "Moscow Russia",
                    new Coordinates(TEST_VALUE_LATLNG_2.getLatitude(), TEST_VALUE_LATLNG_2.getLongitude()),
                    ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5),false),
            new Event(Id.generateRandom(), "Event 3 New York", "New York USA",
                    new Coordinates(TEST_VALUE_LATLNG_3.getLatitude(), TEST_VALUE_LATLNG_3.getLongitude()),
                    ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5),false),
            new Event(Id.generateRandom(), "Event 4 Lagos", "Lagos Nigeria",
                    new Coordinates(TEST_VALUE_LATLNG_4.getLatitude(), TEST_VALUE_LATLNG_4.getLongitude()),
                    ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5),false),
            new Event(Id.generateRandom(), "Event 5 Santiago", "Santiago Chile",
                    new Coordinates(TEST_VALUE_LATLNG_5.getLatitude(), TEST_VALUE_LATLNG_5.getLongitude()),
                    ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5),false),
            new Event(Id.generateRandom(), "Event 6 Los Angeles", "Los Angeles",
                    new Coordinates(TEST_VALUE_LATLNG_6.getLatitude(), TEST_VALUE_LATLNG_6.getLongitude()),
                    ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5),false),
    };

    @Test
    public void testMarkerSymbolManager() throws InterruptedException {
        MarkerSymbolManager markerSymbolManager = getFragmentField("markerSymbolManager", MarkerSymbolManager.class);
        runOnUiThreadAndWaitEndExecution(markerSymbolManager::removeAllMarkers);

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

        getFragmentField("bottomSheetBehavior", BottomSheetBehavior.class).addBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    semaphore.release();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
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
        MarkerSymbolManager markerSymbolManager = getFragmentField("markerSymbolManager", MarkerSymbolManager.class);
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
            Log.d("MapFragmentTest", "map_event_name: " + fragment.getActivity()
                    .findViewById(R.id.fragment_map_event_name));

            onView(withId(R.id.fragment_map_event_name)).check(matches(withText(e.getName())));

            runOnUiThreadAndWaitEndExecution(() ->
                    bsb.setState(BottomSheetBehavior.STATE_HIDDEN)
            );
            waitForBsbHidden.acquire();
        }
    }

}
