package com.github.onedirection.geolocation;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class NominatimGeocodingTest {
    private static final Context CTX = ApplicationProvider.getApplicationContext();
    private static final NominatimGeocoding GEOCODING = new NominatimGeocoding(CTX);

    private static final String EPFL_QUERY = "EPFL";
    private static final List<String> EPFL_NAME = List.of("École Polytechnique Fédérale de Lausanne", "EPFL");
    private static final String EPFL_CANTON = "Vaud";
    private static final List<String> EPFL_COUNTRY = List.of("Switzerland", "Suisse", "Schweiz", "Svizzera", "Svizra");
    private static final Coordinates EPFL_COORDINATES = new Coordinates(46.52, 6.56);
    private static final double EPFL_COORDINATES_PREC = 1e-2;

    private static final String GARBAGE_LOCATION_NAME = "jdfahgfoqaghegaghufagipdhgaofdghaiodgfhoahahid";
    private static final Coordinates GARBAGE_LOCATION_COORDINATEs = new Coordinates(-61.74, -133.41);

    @Test
    public void geocodingHasExpectedResultsForEPFL(){
        try {
            NamedCoordinates coords = GEOCODING.getBestNamedCoordinates(EPFL_QUERY).get();

            assertTrue(EPFL_COORDINATES.areCloseTo(coords, EPFL_COORDINATES_PREC));

            // Those tests might seem overly complicated, but the resulting string
            // seems to vary a bit much.
            String[] elems = coords.name.split(",");
            assertThat(elems[0].trim(), isIn(EPFL_NAME));
            assertThat(coords.name, containsString(EPFL_CANTON));
            assertThat(elems[elems.length-1].split("/")[0].trim(), isIn(EPFL_COUNTRY));
        }
        catch(Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void reverseGeocodingHasExpectedResultForEPFL(){
        try {
            NamedCoordinates coords = GEOCODING.getBestNamedCoordinates(EPFL_COORDINATES).get();

            assertTrue(coords.areCloseTo(EPFL_COORDINATES, EPFL_COORDINATES_PREC));

            String[] elems = coords.name.split(",");
            assertThat(coords.name, containsString(EPFL_CANTON));
            assertThat(elems[elems.length-1].split("/")[0].trim(), isIn(EPFL_COUNTRY));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void geocodingHasNoResultForGarbage() {
        CompletableFuture<Coordinates> result = GEOCODING.getBestCoordinates(GARBAGE_LOCATION_NAME);
        try{
            result.get();
            fail("The result should not contain any value");
        }
        catch(ExecutionException e){
            assertThat(e.getCause(), is(instanceOf(NoSuchElementException.class)));
        }
        catch(InterruptedException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void reverseGeocodingHasNoResultForGarbage(){
        try {
            GEOCODING.getBestNamedCoordinates(GARBAGE_LOCATION_COORDINATEs).get();
        }
        catch(ExecutionException e){
            assertThat(e.getCause(), is(instanceOf(NoSuchElementException.class)));
        }
        catch(InterruptedException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void getBestCoordinatesIsCoherent() throws ExecutionException, InterruptedException {
        assertThat(
                GEOCODING.getBestCoordinates(EPFL_QUERY).get(),
                is(GEOCODING.getBestNamedCoordinates(EPFL_QUERY).get().dropName())
        );
    }
}
