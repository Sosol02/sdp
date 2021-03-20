package com.github.onedirection.geolocalization;

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

    // From https://geohack.toolforge.org/geohack.php?pagename=%C3%89cole_Polytechnique_F%C3%A9d%C3%A9rale_de_Lausanne&params=46_31_13_N_6_33_56_E_region:CH-VD_type:edu
    private static final String EPFL_QUERY = "EPFL";
    private static final List<String> EPFL_NAME = List.of("École Polytechnique Fédérale de Lausanne", "EPFL");
    private static final String EPFL_CANTON = "Vaud";
    private static final List<String> EPFL_COUNTRY = List.of("Switzerland", "Suisse", "Schweiz", "Svizzera", "Svizra");
    private static final Coordinates EPFL_COORDINATES = new Coordinates(46.52, 6.56);
    private static final double EPFL_COORDINATES_PREC = 1e-2;

    private static final String GARBAGE_LOCATION = "jdfahgfoqaghegaghufagipdhgaofdghaiodgfhoahahid";

    @Test
    public void returnsExpectedResultsForEPFL(){
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
    public void returnsEmptyForGarbage() {
        CompletableFuture<Coordinates> result = GEOCODING.getBestCoordinates(GARBAGE_LOCATION);
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
}
