package com.github.onedirection.geocoding;

import java.util.Objects;

import static java.lang.Math.abs;

public final class Coordinates {
    public final double latitude;
    public final double longitude;

    Coordinates(double latitude, double longitude){
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0;
    }

    public boolean areCloseTo(Coordinates that, double tolerance){
        return  abs(this.latitude - that.latitude) < tolerance &&
                abs(this.longitude - that.longitude) < tolerance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
