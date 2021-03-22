package com.github.onedirection.geolocalization;

import android.location.Criteria;
import android.location.LocationManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class AndroidLocationServiceTest {
    private final LocationManager locationManager = (LocationManager) ApplicationProvider.getApplicationContext().getSystemService(ApplicationProvider.getApplicationContext().LOCATION_SERVICE);
    private final Criteria locationCriteria = new Criteria();
    private final String locationProvider = locationManager.getBestProvider(locationCriteria, false);

    private final AndroidLocationService locationService = new AndroidLocationService(locationManager, locationProvider, locationCriteria);

    @Test
    public void buildFromContextAndProviderTest() {
        assertNotNull(AndroidLocationService.buildFromContextAndProvider(ApplicationProvider.getApplicationContext(), locationProvider));
    }

    @Test
    public void buildFromContextAndCriteriaTest() {
        assertNotNull(AndroidLocationService.buildFromContextAndCriteria(ApplicationProvider.getApplicationContext(), locationCriteria));
    }

    @Test
    public void getCurrentLocationTest() {
        if (locationManager.getLastKnownLocation(locationProvider) == null) {
            assertEquals(locationService.getCurrentLocation(), null);
        }
    }
}

