package com.github.onedirection.geocoding;

import android.app.Activity;
import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.onedirection.navigation.NavigationActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class DeviceLocationTest {

    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);
    //private final DeviceLocation deviceLocation = new DeviceLocation(testRule);



    @Test
    public void ableToGetLocationTest(){
        if(deviceLocation.fineLocationUsageIsAllowed()){
            deviceLocation.startLocationTracking();
            Coordinates coordinates = deviceLocation.getLastLocation();
            assertNotNull(coordinates);
        }
    }
}
