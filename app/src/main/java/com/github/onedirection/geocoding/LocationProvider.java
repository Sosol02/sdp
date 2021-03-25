package com.github.onedirection.geocoding;

public interface LocationProvider {

    public boolean startLocationTracking();
    public boolean fineLocationUsageIsAllowed();
    public Coordinates getLastLocation();
    public void requestFineLocationPermission();

}
