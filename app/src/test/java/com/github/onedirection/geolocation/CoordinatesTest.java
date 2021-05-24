package com.github.onedirection.geolocation;

import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.geolocation.model.NamedCoordinates;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertTrue;

public class CoordinatesTest {
    private final static double LAT = 4.56;
    private final static double LON = 12.34;
    private final static String NAME = "PifPafPouf";

    private final static double TOL = 1e-12;

    @Test
    public void toStringContainsCoordinatesAndName(){
        String str1 = (new Coordinates(LAT, LON)).toString();
        String str2 = (new NamedCoordinates(LAT, LON, NAME)).toString();

        assertThat(str1, containsString(Double.toString(LAT)));
        assertThat(str1, containsString(Double.toString(LON)));
        assertThat(str2, containsString(Double.toString(LAT)));
        assertThat(str2, containsString(Double.toString(LON)));
        assertThat(str2, containsString(NAME));
    }

    @Test
    public void equalsBehavesCorrectly(){
        Coordinates c1 = new Coordinates(LAT, LON);
        Coordinates c2 = new Coordinates(LAT, LON+1);
        NamedCoordinates nc1 = c1.addName(NAME);
        NamedCoordinates nc2 = c2.addName(NAME);
        NamedCoordinates nc3 = c1.addName("");
        int i = 4;

        assertThat(c1, is(c1));
        assertThat(c2, is(c2));
        assertThat(c1, not(c2));
        assertThat(c1, not(i));
        assertThat(c1, not(nc1));

        assertThat(nc1, is(nc1));
        assertThat(nc2, is(nc2));
        assertThat(nc3, is(nc3));
        assertThat(nc1, not(nc2));
        assertThat(nc1, not(nc3));
        assertThat(nc2, not(nc3));
    }

    @Test
    public void hashCodeIsEqualCompatible(){
        Coordinates c1 = new Coordinates(LAT, LON);
        Coordinates c2 = new Coordinates(LAT, LON);
        NamedCoordinates nc1 = c1.addName(NAME);
        NamedCoordinates nc2 = new NamedCoordinates(LAT, LON, NAME);

        assertThat(c1.hashCode(), is(c2.hashCode()));
        assertThat(nc1.hashCode(), is(nc2.hashCode()));
    }

    @Test
    public void behaviorIsCoherentOnceNamed(){
        Coordinates c = new Coordinates(LAT, LON);
        NamedCoordinates nc = c.addName(NAME);

        assertTrue(c.areCloseTo(c, TOL));
        assertTrue(c.areCloseTo(nc, TOL));
        assertTrue(nc.areCloseTo(c, TOL));
        assertTrue(nc.areCloseTo(nc, TOL));
    }
}
