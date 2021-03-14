package com.github.onedirection.geocoding;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

public class CoordinatesTest {
    private final static double LAT = 4.56;
    private final static double LON = 12.34;

    @Test
    public void toStringContainsCoordinates(){
        String str = (new Coordinates(LAT, LON)).toString();

        assertThat(str, containsString(Double.toString(LAT)));
        assertThat(str, containsString(Double.toString(LON)));
    }

    @Test
    public void equalsBehavesCorrectly(){
        Coordinates c1 = new Coordinates(LAT, LON);
        Coordinates c2 = new Coordinates(LAT, LON+1);
        int i = 4;

        assertThat(c1, is(c1));
        assertThat(c2, is(c2));
        assertThat(c1, not(c2));
        assertThat(c1, not(i));
    }

    @Test
    public void hashCodeIsEqualCompatible(){
        Coordinates c1 = new Coordinates(LAT, LON);
        Coordinates c2 = new Coordinates(LAT, LON);

        assertThat(c1.hashCode(), is(c2.hashCode()));
    }
}
