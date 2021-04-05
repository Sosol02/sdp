package com.github.onedirection.map;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.Event;
import com.github.onedirection.R;
import com.github.onedirection.WaitAction;
import com.github.onedirection.navigation.fragment.map.MapFragment;
import com.github.onedirection.navigation.fragment.map.MarkerSymbolManager;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.Pair;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)
public class MapFragmentTest {

    private MapboxMap mapboxMap;
    private MapFragment fragment;
    private OnMapReadyIdlingResource onMapReadyIdlingResource;
    private FragmentScenario<MapFragment> scenario;
    private final LatLng TEST_VALUE_LATLNG_1 = new LatLng(2f, 0.003f);
    private final LatLng TEST_VALUE_LATLNG_2 = new LatLng(34f, 0.1543f);
    private final LatLng TEST_VALUE_LATLNG_3 = new LatLng(20f, 09583f);
    private final Event TEST_EVENT_1 = new Event(Id.generateRandom(), "Test event", "Paris",
            ZonedDateTime.of(2021, 4, 2, 13, 42, 56, 0, ZoneId.systemDefault()),
            ZonedDateTime.of(2021, 4, 2, 13, 58, 56, 0, ZoneId.systemDefault()));

    @Before
    public void setupForTest() {
        scenario = FragmentScenario.launchInContainer(MapFragment.class);
        scenario.onFragment(fragment -> {
            this.fragment = fragment;
            onMapReadyIdlingResource = new OnMapReadyIdlingResource(fragment);
        });
        IdlingRegistry.getInstance().register(onMapReadyIdlingResource);
        onView(withId(R.id.mapView)).check(matches(isDisplayed()));
        mapboxMap = onMapReadyIdlingResource.getMapboxMap();
    }

    @After
    public void AtEndTest() {
        IdlingRegistry.getInstance().unregister(onMapReadyIdlingResource);
    }

    @Test
    public void isOnMapReadyIdlingResourceWorking() {
        assertThat(mapboxMap, is(notNullValue()));
        assertThat(getMarkerSymbolManager(), is(notNullValue()));
    }

    @Test
    public void testMarkerSymbolManager() {
        onView(withId(R.id.mapView)).perform(new WaitAction(1000));
        MarkerSymbolManager markerSymbolManager = getMarkerSymbolManager();

        final Symbol[] marker = new Symbol[1];
        fragment.getActivity().runOnUiThread(() -> marker[0] = markerSymbolManager.addMarker(TEST_VALUE_LATLNG_1));
        onView(withId(R.id.mapView)).perform(new WaitAction(1000));

        List<Symbol> markers = markerSymbolManager.getAllMarkers();
        assertThat(markers.size(), is(1));
        assertThat(markers.get(0).getLatLng(), is(TEST_VALUE_LATLNG_1));
        fragment.getActivity().runOnUiThread(() -> markerSymbolManager.removeMarker(marker[0]));
        onView(withId(R.id.mapView)).perform(new WaitAction(1000));
        markers = markerSymbolManager.getAllMarkers();
        assertThat(markers.size(), is(0));

        fragment.getActivity().runOnUiThread(() -> markerSymbolManager.addMarkers(TEST_VALUE_LATLNG_2, TEST_VALUE_LATLNG_3));
        onView(withId(R.id.mapView)).perform(new WaitAction(1000));

        markers = markerSymbolManager.getAllMarkers();
        assertThat(markers.size(), is(2));
        fragment.getActivity().runOnUiThread(() -> markerSymbolManager.removeAllMarkers());
        onView(withId(R.id.mapView)).perform(new WaitAction(1000));
        assertThat(markers.size(), is(0));
    }

    @Test
    public void testClickOnMapPutAMarkerOnTheMap() {
        onView(withId(R.id.mapView)).perform(click());
        onView(withId(R.id.mapView)).perform(new WaitAction(1000));
        MarkerSymbolManager markerSymbolManager = getMarkerSymbolManager();
        List<Symbol> markers = markerSymbolManager.getAllMarkers();
        assertThat(markers.size(), is(1));
        onView(withId(R.id.mapView)).perform(click());
        markers = markerSymbolManager.getAllMarkers();
        assertThat(markers.size(), is(1));
    }

    @Test
    public void testAddEventPutsMarkerOnMap() {
        MarkerSymbolManager markerSymbolManager = getMarkerSymbolManager();
        Pair<Symbol, LatLng> pair;
        try {
            pair = markerSymbolManager.addGeocodedEventMarker(TEST_EVENT_1).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BottomSheetBehavior<View> bsb = getBottomSheetBehavior();
        assertThat(bsb.getState(), is(BottomSheetBehavior.STATE_HIDDEN));

        fragment.getActivity().runOnUiThread(() -> {
            // need to zoom to center the marker and make the next click() click it
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pair.second, 15));
        });

        onView(withId(R.id.mapView))
                .perform(new WaitAction(500)) // wait for animation to end
                .perform(click())
                .perform(new WaitAction(1000));

        assertThat(bsb.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
        onView(withId(R.id.fragment_map_event_name)).check(matches(withText(TEST_EVENT_1.getName())));
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

    private BottomSheetBehavior<View> getBottomSheetBehavior() {
        try {
            Field field = fragment.getClass().getDeclaredField("bottomSheetBehavior");
            field.setAccessible(true);
            return (BottomSheetBehavior<View>) field.get(fragment);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
