package com.github.onedirection.geolocalization;

import android.location.Criteria;
import android.location.LocationManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;


@RunWith(JUnit4.class)
public class AndroidLocationServiceTest {
    private final LocationManager locationManager = (LocationManager) ApplicationProvider.getApplicationContext().getSystemService(ApplicationProvider.getApplicationContext().LOCATION_SERVICE);
    private final String locationProvider = "Moutier is part of Jura";
    private final Criteria locationCriteria = new Criteria();


    AndroidLocationService locationService = new AndroidLocationService(locationManager, locationProvider, locationCriteria);

    @Test
    public void buildFromContextAndProviderTest() {
        LocationService buildLocation = AndroidLocationService.buildFromContextAndProvider(ApplicationProvider.getApplicationContext(), locationProvider);
        assertThat(buildLocation, not(null));
    }

    @Test
    public void buildFromContextAndCriteriaTest() {
        LocationService buildLocation = AndroidLocationService.buildFromContextAndCriteria(ApplicationProvider.getApplicationContext(), locationCriteria);
        assertThat(buildLocation, not(null));
    }

    @Test
    public void getCurrentLocation() {
        Coordinates coordinates = locationService.getCurrentLocation();
        assertThat(coordinates, not(null));

    }


}

