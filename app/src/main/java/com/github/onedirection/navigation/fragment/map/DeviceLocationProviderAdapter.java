package com.github.onedirection.navigation.fragment.map;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.onedirection.geolocation.location.DeviceLocationProvider;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.model.location.Location;

/**
 * Class that adapt our deviceLocationProviderAdapter to the LocationProviderAdapter
 * used for the Navigation Manager of Map Quest
*/

public class DeviceLocationProviderAdapter extends LocationProviderAdapter {

    private final DeviceLocationProvider deviceLocationProvider;

    public DeviceLocationProviderAdapter(DeviceLocationProvider deviceLocationProvider) {
        this.deviceLocationProvider = deviceLocationProvider;
        deviceLocationProvider.setLocationCallBackNavigation(new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = androidLocationToMapquestLocation(locationResult.getLastLocation());
                if (location != null) {
                    notifyListenersLocationChanged(location);
                }
            }
        });
        requestLocationUpdates();
    }

    @Override
    public void requestLocationUpdates() {
        deviceLocationProvider.startLocationTracking();
    }

    @Override
    protected void cancelLocationUpdates() {
        deviceLocationProvider.stopLocationTracking();
    }

    @Nullable
    @Override
    public Location getLastKnownLocation() {
        return androidLocationToMapquestLocation(deviceLocationProvider.getLastAndroidLocation());
    }

    public Location androidLocationToMapquestLocation(android.location.Location location) {
        return new Location(location.getLatitude(),
                location.getLongitude(), location.getAltitude(), location.getBearing(),
                location.getSpeed(), location.getAccuracy(), location.getTime());
    }


    /*DeviceLocationProvider is not parcelable, so we cannot implement the parcelable interface correctly,
    therefore we throw an exception when used */

    @Override
    public String getLocationProviderId() {
        return "device-location-provider";
    }

    @Override
    public int describeContents() {
        throw new UnsupportedOperationException("DeviceLocationProvider is not parcelable");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException("DeviceLocationProvider is not parcelable");
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public DeviceLocationProviderAdapter createFromParcel(Parcel in) {
            throw new UnsupportedOperationException("DeviceLocationProvider is not parcelable");
        }

        @Override
        public DeviceLocationProviderAdapter[] newArray(int size) {
            throw new UnsupportedOperationException("DeviceLocationProvider is not parcelable");
        }
    };
}
