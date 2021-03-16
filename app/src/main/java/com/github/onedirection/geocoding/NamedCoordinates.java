package com.github.onedirection.geocoding;

import java.util.Objects;

import static java.lang.Math.abs;

/** Represents (immutable) geographic coordinates paired with the name of the location. */
public class NamedCoordinates {
    public final double latitude;
    public final double longitude;
    public final String name;

    public NamedCoordinates(double latitude, double longitude, String name){
        Objects.requireNonNull(name);
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public NamedCoordinates(Coordinates coordinates, String name){
        this(coordinates.latitude, coordinates.longitude, name);
    }

    public Coordinates dropName(){
        return new Coordinates(latitude, longitude);
    }

    @Override
    public String toString() {
        return name + "(" +
                "lat=" + latitude +
                ", lon=" + longitude +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NamedCoordinates that = (NamedCoordinates) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                that.name.equals(name);
    }

    /**
     * Checks whether two coordinates are close enough.
     * @param that The other coordinates.
     * @param tolerance The maximal difference for each component.
     * @return True if the two coordinates are close.
     */
    public boolean areCloseTo(Coordinates that, double tolerance){
        return  dropName().areCloseTo(that, tolerance);
    }

    /**
     * Checks whether two coordinates are close enough.
     * @param that The other coordinates.
     * @param tolerance The maximal difference for each component.
     * @return True if the two coordinates are close.
     */
    public boolean areCloseTo(NamedCoordinates that, double tolerance){
        return  dropName().areCloseTo(that.dropName(), tolerance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, name);
    }
}
