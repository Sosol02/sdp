package com.github.onedirection.navigation.fragment.map;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.github.onedirection.geolocation.location.AbstractDeviceLocationProvider;
import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.model.location.Location;

public class DeviceLocationProviderAdapter extends LocationProviderAdapter {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public DeviceLocationProviderAdapter createFromParcel(Parcel in) {
            throw new IllegalStateException("DeviceLocationprovider is not parcelable");
        }

        @Override
        public DeviceLocationProviderAdapter[] newArray(int size) {
            throw new IllegalStateException("DeviceLocationprovider is not parcelable");
        }
    };

    private AbstractDeviceLocationProvider deviceLocationProvider;

    public DeviceLocationProviderAdapter(AbstractDeviceLocationProvider deviceLocationProvider) {
        this.deviceLocationProvider = deviceLocationProvider;
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
        android.location.Location location = deviceLocationProvider.getLastAndroidLocation();
        Location locationMapQuest = new Location(location.getLatitude(),
                location.getLongitude(), location.getAltitude(), location.getBearing(),
                location.getSpeed(), location.getAccuracy(), location.getTime());
        return locationMapQuest;
    }

    @Override
    public String getLocationProviderId() {
        return "device-location-provider";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}
}
