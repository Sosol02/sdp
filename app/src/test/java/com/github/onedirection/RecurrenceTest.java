package com.github.onedirection;

import com.github.onedirection.events.Event;
import com.github.onedirection.events.Recurrence;
import com.github.onedirection.utils.Id;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RecurrenceTest {

    private final static long SECONDS_WEEK = 3600*24*7;
    private final static Id GROUP_ID = Id.generateRandom();
    private final static Duration DURATION_WEEK = Duration.ofDays(7);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
    private final static Recurrence RECURRENCE = new Recurrence(GROUP_ID, DURATION_WEEK, END_TIME);

    @Test
    public void testRecurrenceWithNullArgument() {
        assertThrows(NullPointerException.class, () -> new Recurrence(null, DURATION_WEEK, END_TIME));
        assertThrows(NullPointerException.class, () -> new Recurrence(GROUP_ID, null, END_TIME));
        assertThrows(NullPointerException.class, () -> new Recurrence(GROUP_ID, DURATION_WEEK, null));
    }

    @Test
    public void gettersAreCorrect() {
        assertEquals(GROUP_ID, RECURRENCE.getGroupId());
        assertEquals(DURATION_WEEK, RECURRENCE.getPeriod());
        assertEquals(END_TIME, RECURRENCE.getEndTime());
    }

    @Test
    public void durationFromSecondsIsCorrect() {
        Recurrence fromSeconds = new Recurrence(GROUP_ID, SECONDS_WEEK, END_TIME);
        assertEquals(RECURRENCE, fromSeconds);
    }

    @Test
    public void durationToSecondsAndReverseGiveSameResults() {
        Duration d = Duration.ofSeconds(SECONDS_WEEK); //Week
        long secondsFromDuration = d.getSeconds();
        assertEquals(SECONDS_WEEK, secondsFromDuration);
    }

    @Test
    public void testSetPeriodAndGet() {
        final Duration newPeriod = Duration.ofHours(24); //1 day

        assertThrows(NullPointerException.class, () -> RECURRENCE.setPeriod(null));
        Recurrence recurrChanged = RECURRENCE.setPeriod(newPeriod);
        assertEquals(newPeriod, recurrChanged.getPeriod());
        assertEquals(DURATION_WEEK, RECURRENCE.getPeriod());
        assertThat(RECURRENCE.setPeriod(DURATION_WEEK), sameInstance(RECURRENCE));
        assertThat(RECURRENCE.setPeriodFromSeconds(SECONDS_WEEK), sameInstance(RECURRENCE));
    }

    @Test
    public void testSetEndTimeAndGet() {
        final ZonedDateTime newEndTime = END_TIME.plusDays(4);

        assertThrows(NullPointerException.class, () -> RECURRENCE.setEndTime(null));
        Recurrence recurrChanged = RECURRENCE.setEndTime(newEndTime);
        assertEquals(newEndTime, recurrChanged.getEndTime());
        assertEquals(END_TIME, RECURRENCE.getEndTime());
        assertThat(RECURRENCE.setEndTime(END_TIME), sameInstance(RECURRENCE));
    }

    @Test
    public void toStringContainsAllFields() {
        String str = RECURRENCE.toString();
        assertThat(str, containsString(GROUP_ID.toString()));
        assertThat(str, containsString(DURATION_WEEK.toString()));
        assertThat(str, containsString(END_TIME.toString()));
    }

    @Test
    public void equalsBehavesAsExpected() {
        Recurrence recurr1 = new Recurrence(GROUP_ID, DURATION_WEEK, END_TIME);
        assertThat(RECURRENCE, is(RECURRENCE));
        assertThat(RECURRENCE, is(recurr1));
        Recurrence recurr2 = new Recurrence(Id.generateRandom(), DURATION_WEEK, END_TIME);
        assertThat(RECURRENCE, not(is(recurr2)));
        Recurrence recurr3 = new Recurrence(GROUP_ID, SECONDS_WEEK, END_TIME);
        assertThat(RECURRENCE, is(recurr3));
        Recurrence recurr4 = new Recurrence(GROUP_ID, DURATION_WEEK.minusMinutes(4), END_TIME);
        assertThat(RECURRENCE, not(is(recurr4)));
        Recurrence recurr5 = new Recurrence(GROUP_ID, DURATION_WEEK, END_TIME.plusHours(2));
        assertThat(RECURRENCE, not(is(recurr5)));
    }

    @Test
    public void hashCodeIsEqualCompatible(){
        Recurrence event = new Recurrence(GROUP_ID, DURATION_WEEK, END_TIME);
        assertThat(event, is(RECURRENCE));
        assertThat(event, not(sameInstance(RECURRENCE)));
        assertThat(event.hashCode(), is(RECURRENCE.hashCode()));
    }
}
