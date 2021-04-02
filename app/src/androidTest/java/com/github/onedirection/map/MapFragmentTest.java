package com.github.onedirection.map;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.WaitAction;
import com.github.onedirection.navigation.fragment.map.MapFragment;
import com.github.onedirection.navigation.fragment.map.MarkerSymbolManager;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
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

    private MarkerSymbolManager getMarkerSymbolManager() {
        try {
            Field field = fragment.getClass().getDeclaredField("markerSymbolManager");
            field.setAccessible(true);
            return ((MarkerSymbolManager) field.get(fragment));
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }
}
