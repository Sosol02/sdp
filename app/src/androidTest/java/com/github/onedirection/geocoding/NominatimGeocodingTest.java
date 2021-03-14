package com.github.onedirection.geocoding;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.github.onedirection.utils.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class NominatimGeocodingTest {
    private static final Context CTX = ApplicationProvider.getApplicationContext();
    private static final NominatimGeocoding GEOCODING = new NominatimGeocoding(CTX);

    // From https://geohack.toolforge.org/geohack.php?pagename=%C3%89cole_Polytechnique_F%C3%A9d%C3%A9rale_de_Lausanne&params=46_31_13_N_6_33_56_E_region:CH-VD_type:edu
    private static final String EPFL_NAME = "EPFL";
    private static final Coordinates EPFL_COORDINATES = new Coordinates(46.52, 6.56);
    private static final double EPFL_COORDINATES_PREC = 1e-2;

    private static final String GARBAGE_LOCATION = "jdfahgfoqaghegaghufagipdhgaofdghaiodgfhoahahid";

    @Test
    public void returnsExpectedResultsForEPFL(){
        try {
            Coordinates coords = GEOCODING.getBestCoordinates(EPFL_NAME).get();

            assertTrue(EPFL_COORDINATES.areCloseTo(coords, EPFL_COORDINATES_PREC));
        }
        catch(Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void returnsEmptyForGarbage() {
        CompletableFuture<Pair<Coordinates, String>> result = GEOCODING.getBestNamedCoordinates(GARBAGE_LOCATION);
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
