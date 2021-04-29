package com.github.onedirection.geolocation;

import android.Manifest;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;

import com.github.onedirection.geolocation.location.DeviceLocationProvider;
import com.github.onedirection.geolocation.location.DeviceLocationProviderNoRequests;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeviceLocationTest {


    @Rule
    public GrantPermissionRule grantPermissionRule =
            GrantPermissionRule.grant(
                    Manifest.permission.ACCESS_FINE_LOCATION);

    @Test
    public void noRequestsDoesNotProduceRequests() throws InterruptedException, ExecutionException, TimeoutException {
        DeviceLocationProvider provider = new DeviceLocationProviderNoRequests(ApplicationProvider.getApplicationContext());

        // Ensures that no exception is thrown
        provider.requestFineLocationPermission().get(0, TimeUnit.NANOSECONDS);
    }
}
