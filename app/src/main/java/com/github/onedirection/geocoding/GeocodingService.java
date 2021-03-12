package com.github.onedirection.geocoding;

import com.github.onedirection.utils.Pair;

import java.util.Optional;

interface GeocodingService {

    Optional<Coordinates> getBestCoordinates(String locationName);

    Optional<Pair<Coordinates, String>> getBestNamedCoordinates(String locationName);

}
