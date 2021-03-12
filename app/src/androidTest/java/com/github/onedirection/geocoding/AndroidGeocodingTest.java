package com.github.onedirection.geocoding;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.github.onedirection.utils.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class AndroidGeocodingTest {
    private static final Context CTX = ApplicationProvider.getApplicationContext();
    private static final AndroidGeocoding GEOCODING = AndroidGeocoding.fromContext(CTX);

    // From https://geohack.toolforge.org/geohack.php?pagename=%C3%89cole_Polytechnique_F%C3%A9d%C3%A9rale_de_Lausanne&params=46_31_13_N_6_33_56_E_region:CH-VD_type:edu
    private static final String EPFL_NAME = "École Polytechnique Fédérale de Lausanne";
    private static final Coordinates EPFL_COORDINATES = new Coordinates(46.52, 6.56);
    private static final double EPFL_COORDINATES_PREC = 1e-2;

    private static final String GARBAGE_LOCATION = "jdfahgfoqaghegaghufagipdhgaofdghaiodgfhoahahid";

    @Test
    public void returnsExpectedResultsForEPFL(){
        Optional<Pair<Coordinates, String>> coords = GEOCODING.getBestNamedCoordinates(EPFL_NAME);

        assertTrue(coords.isPresent());
        assertTrue(EPFL_COORDINATES.isCloseTo(coords.get().first, EPFL_COORDINATES_PREC));
    }

    @Test
    public void returnsEmptyForGarbage(){
        assertFalse(GEOCODING.getBestNamedCoordinates(GARBAGE_LOCATION).isPresent());
    }
}
