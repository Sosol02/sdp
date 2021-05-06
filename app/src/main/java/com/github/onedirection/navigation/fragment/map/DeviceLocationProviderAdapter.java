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

    private DeviceLocationProvider deviceLocationProvider;

    public DeviceLocationProviderAdapter(DeviceLocationProvider deviceLocationProvider) {
        this.deviceLocationProvider = deviceLocationProvider;
        deviceLocationProvider.setLocationCallBackNavigation(new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = androidLocationToMapquestLocation(locationResult.getLastLocation());
                notifyListenersLocationChanged(location);
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

    @Override
    public String getLocationProviderId() {
        return "device-location-provider";
    }

    @Override
    public int describeContents() {
        throw new IllegalStateException("DeviceLocationprovider is not parcelable");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new IllegalStateException("DeviceLocationprovider is not parcelable");
    }

    public Location androidLocationToMapquestLocation(android.location.Location location) {
        return new Location(location.getLatitude(),
                location.getLongitude(), location.getAltitude(), location.getBearing(),
                location.getSpeed(), location.getAccuracy(), location.getTime());
    }
}
