package com.github.onedirection.map;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.navigation.fragment.map.MyLocationSymbolManager;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Semaphore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MyLocationSymbolManagerTest extends MapFragmentTestSetup{

    @Test
    public void testMyLocationIsAppearing() throws InterruptedException {
        MyLocationSymbolManager myLocationSymbolManager = getFragmentField("myLocationSymbolManager", MyLocationSymbolManager.class);
        setupDeviceLocationProviderMock(true);
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
        setupDeviceLocationProviderMock(true);
        onView(withId(R.id.my_location_button)).perform(click());
        assertThat(myLocationSymbolManager.getPosition(), is(notNullValue()));
    }

    @Test
    public void testMyLocationIsNotAppearingWhenNotAuthorized() {
        MyLocationSymbolManager myLocationSymbolManager = getFragmentField("myLocationSymbolManager", MyLocationSymbolManager.class);
        setupDeviceLocationProviderMock(false);
        Symbol myLocation = getAttributeField("myLocation", myLocationSymbolManager, Symbol.class);
        assertTrue(myLocation == null || myLocation.getIconOpacity() == 0.0f);
    }

    private void setupDeviceLocationProviderMock(boolean locationPermission) {
        MyLocationSymbolManager myLocationSymbolManager = getFragmentField("myLocationSymbolManager", MyLocationSymbolManager.class);
        DeviceLocationProviderMock deviceLocationProviderMock = new DeviceLocationProviderMock(locationPermission);
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
    }
}
