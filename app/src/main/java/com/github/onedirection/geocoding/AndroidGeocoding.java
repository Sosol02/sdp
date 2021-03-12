package com.github.onedirection.geocoding;

import android.content.Context;
import android.location.Geocoder;

import com.github.onedirection.utils.Pair;

import java.io.IOException;
import java.util.Optional;

public final class AndroidGeocoding implements GeocodingService {
    public final static int DEFAULT_MAX_RESULTS = 5;
    private final Geocoder geocoder;
    private final int maxResults;

    public AndroidGeocoding(Geocoder geocoder, int maxResults){
        this.geocoder = geocoder;
        this.maxResults =  maxResults;
    }

    public AndroidGeocoding(Geocoder geocoder){
        this(geocoder, DEFAULT_MAX_RESULTS);
    }

    // Can be activated if the max result is ever used
//    public static final AndroidGeocoding fromContext(Context context, int maxResults) {
//        return new AndroidGeocoding(new Geocoder(context), maxResults);
//    }

    public static final AndroidGeocoding fromContext(Context context) {
        return new AndroidGeocoding(new Geocoder(context));
    }

    @Override
    public Optional<Coordinates> getBestCoordinates(String locationName) {
        Optional<Pair<Coordinates, String>> r = getBestNamedCoordinates(locationName);
        return r.isPresent() ?
            Optional.of(r.get().first) :
            Optional.empty();
    }

    @Override
    public Optional<Pair<Coordinates, String>> getBestNamedCoordinates(String locationName) {
        try {
            return this.geocoder.getFromLocationName(locationName, maxResults)
                    .stream()
                    .filter(adr -> adr.hasLatitude() && adr.hasLongitude())
                    .map(adr -> new Pair<>(new Coordinates(adr.getLatitude(), adr.getLongitude()), adr.getFeatureName()))
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
