package com.github.onedirection.utils;

import com.github.onedirection.geocoding.Coordinates;

import org.junit.Test;

import java.util.Date;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

public class PairTest {
    private static final Optional<Integer> FIRST = Optional.of(5);
    private static final Date SECOND = new Date();

    @Test
    public void toStringContainsElements(){
        String str = (new Pair<>(FIRST, SECOND)).toString();

        assertThat(str, containsString(FIRST.toString()));
        assertThat(str, containsString(SECOND.toString()));
    }

    @Test
    public void equalsBehavesCorrectly(){
        Pair<Optional<Integer>, Date> c1 = new Pair<>(FIRST, SECOND);
        Pair<Optional<Integer>, Date> c2 = new Pair<>(Optional.empty(), SECOND);
        Pair<Optional<Integer>, Date> c3 = new Pair<>(FIRST, new Date(0));
        int i = 4;

        assertThat(c1, is(c1));
        assertThat(c2, is(c2));
        assertThat(c3, is(c3));
        assertThat(c1, not(c2));
        assertThat(c1, not(c3));
        assertThat(c1, not(i));
    }

    @Test
    public void hashCodeIsEqualCompatible(){
        Pair<Optional<Integer>, Date> c1 = new Pair<>(FIRST, SECOND);
        Pair<Optional<Integer>, Date> c2 = new Pair<>(FIRST, SECOND);

        assertThat(c1.hashCode(), is(c2.hashCode()));
    }
}
