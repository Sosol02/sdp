package com.github.onedirection.geocoding;

import android.app.Activity;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@RunWith(MockitoJUnitRunner.class)
public class DeviceLocationProviderTest {

    @Mock
    FusedLocationProviderClient fusedProvider = mock(FusedLocationProviderClient.class);

    private static MockedStatic<Looper> looper = mockStatic(Looper.class);

    @BeforeClass
    public static void mockitoSetup(){
        looper.when(Looper::getMainLooper).thenReturn(null);
    }

    @Test
    public void returnsLocationWithSingleInstantLocation(){
        Activity activity = new Activity();
        DeviceLocationProvider.getCurrentLocation(activity);
    }

    @AfterClass
    public static void mockitoCleanup(){
        looper.close();
    }

}
