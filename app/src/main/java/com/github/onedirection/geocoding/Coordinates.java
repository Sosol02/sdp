package com.github.onedirection.geocoding;

import java.util.Objects;

import static java.lang.Math.abs;

public final class Coordinates {
    public final double x;
    public final double y;

    Coordinates(double x, double y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Coordinates(" +
                "x=" + x +
                ", y=" + y +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0;
    }

    public boolean isCloseTo(Coordinates that, double tolerance){
        return  abs(this.x - that.x) < tolerance &&
                abs(this.y - that.y) < tolerance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
