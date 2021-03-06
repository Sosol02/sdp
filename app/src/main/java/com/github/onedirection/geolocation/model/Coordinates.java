package com.github.onedirection.geolocation.model;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import static java.lang.Math.abs;

/** Represents (immutable) geographic coordinates. */
@Immutable
public final class Coordinates implements Serializable {
    public final double latitude;
    public final double longitude;

    public Coordinates(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Coordinates(" +
                "lat=" + latitude +
                ", lon=" + longitude +
                ')';
    }

    public NamedCoordinates addName(String name){
        return new NamedCoordinates(this, name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0;
    }

    /**
     * Checks whether two coordinates are close enough.
     * @param that The other coordinates.
     * @param tolerance The maximal difference for each component.
     * @return True if the two coordinates are close.
     */
    public boolean areCloseTo(Coordinates that, double tolerance){
        return  abs(this.latitude - that.latitude) < tolerance &&
                abs(this.longitude - that.longitude) < tolerance;
    }

    /**
     * Checks whether two coordinates are close enough.
     * @param that The other coordinates.
     * @param tolerance The maximal difference for each component.
     * @return True if the two coordinates are close.
     */
    public boolean areCloseTo(NamedCoordinates that, double tolerance){
        return  areCloseTo(that.dropName(), tolerance);
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
