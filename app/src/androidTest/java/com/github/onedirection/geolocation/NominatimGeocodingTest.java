package com.github.onedirection.geolocation;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.github.onedirection.utils.Monads;

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
import static org.hamcrest.Matchers.lessThanOrEqualTo;
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
    private static final int QUERY_COUNT = 20;

    private void assertValidResultForEPFL(NamedCoordinates coordinates, boolean withName){
        assertTrue(EPFL_COORDINATES.areCloseTo(coordinates, EPFL_COORDINATES_PREC));

        // Those tests might seem overly complicated, but the resulting string
        // seems to vary a bit much.
        String[] elems = coordinates.name.split(",");
        if(withName)
            assertThat(elems[0].trim(), isIn(EPFL_NAME));
        assertThat(coordinates.name, containsString(EPFL_CANTON));
        assertThat(elems[elems.length-1].split("/")[0].trim(), isIn(EPFL_COUNTRY));
    }

    @Test
    public void geocodingHasExpectedResultsForEPFL(){
        try {
            assertValidResultForEPFL(GEOCODING.getBestNamedCoordinates(EPFL_QUERY).get(), true);
        }
        catch(Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void allGeocodingResultsHaveExpectedResultsForEPFL(){
        try {
            List<NamedCoordinates> results = GEOCODING.getNamedCoordinates(EPFL_QUERY, QUERY_COUNT).get();
            assertThat(results.size(), is(lessThanOrEqualTo(QUERY_COUNT)));
            results.forEach(r -> assertValidResultForEPFL(r, true));
        }
        catch(Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void cannotRequestNoResults() {
        try{
            GEOCODING.getNamedCoordinates(EPFL_QUERY, 0);
            fail("Should have thrown");
        }
        catch(Exception e){
            assertThat(e, is(instanceOf(IllegalArgumentException.class)));
        }
    }

    @Test
    public void reverseGeocodingHasExpectedResultForEPFL(){
        try {
            assertValidResultForEPFL(GEOCODING.getBestNamedCoordinates(EPFL_COORDINATES).get(), false);
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
        assertThat(
                GEOCODING.getCoordinates(EPFL_QUERY, QUERY_COUNT).get(),
                is(Monads.map(GEOCODING.getNamedCoordinates(EPFL_QUERY, QUERY_COUNT).get(), NamedCoordinates::dropName))
        );
    }
}
