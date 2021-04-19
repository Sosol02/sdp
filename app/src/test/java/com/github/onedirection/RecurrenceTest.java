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
    private final static Id PREV_EVENT_ID = Id.generateRandom();
    private final static Id NEXT_EVENT_ID = Id.generateRandom();
    private final static Recurrence RECURRENCE = new Recurrence(GROUP_ID, DURATION_WEEK, Optional.of(PREV_EVENT_ID), Optional.of(NEXT_EVENT_ID));

    @Test
    public void testRecurrenceWithNullArgument() {
        assertThrows(NullPointerException.class, () -> new Recurrence(null, DURATION_WEEK, Optional.of(PREV_EVENT_ID), Optional.of(NEXT_EVENT_ID)));
        assertThrows(NullPointerException.class, () -> new Recurrence(GROUP_ID, null, Optional.of(PREV_EVENT_ID), Optional.of(NEXT_EVENT_ID)));
        assertThrows(NullPointerException.class, () -> new Recurrence(GROUP_ID, DURATION_WEEK, null, Optional.of(NEXT_EVENT_ID)));
        assertThrows(NullPointerException.class, () -> new Recurrence(GROUP_ID, DURATION_WEEK, Optional.of(PREV_EVENT_ID), null));
    }

    @Test
    public void gettersAreCorrect() {
        assertEquals(GROUP_ID, RECURRENCE.getGroupId());
        assertEquals(DURATION_WEEK, RECURRENCE.getPeriod());
        assertEquals(PREV_EVENT_ID, RECURRENCE.getPrevEvent().orElse(null));
        assertEquals(NEXT_EVENT_ID, RECURRENCE.getNextEvent().orElse(null));
    }

    @Test
    public void durationFromSecondsIsCorrect() {
        Recurrence fromSeconds = new Recurrence(GROUP_ID, SECONDS_WEEK, Optional.of(PREV_EVENT_ID), Optional.of(NEXT_EVENT_ID));
        assertEquals(RECURRENCE, fromSeconds);
    }

    @Test
    public void durationToSecondsAndReverseGiveSameResults() {
        Duration d = Duration.ofSeconds(SECONDS_WEEK); //Week
        long secondsFromDuration = d.getSeconds();
        assertEquals(SECONDS_WEEK, secondsFromDuration);
    }

    @Test
    public void recurrenceHasNullEventPointers() {
        Recurrence nullEventPointers = new Recurrence(GROUP_ID, DURATION_WEEK, Optional.ofNullable(null), Optional.ofNullable(null));
        assertEquals(Optional.empty(), nullEventPointers.getPrevEvent());
        assertEquals(Optional.empty(), nullEventPointers.getNextEvent());
    }

    @Test
    public void testSetGroupIdAndGet() {
        final Id newId = Id.generateRandom();

        assertThrows(NullPointerException.class, () -> RECURRENCE.setGroupId(null));
        Recurrence recurrChanged = RECURRENCE.setGroupId(newId);
        assertEquals(newId, recurrChanged.getGroupId());
        assertEquals(GROUP_ID, RECURRENCE.getGroupId());
        assertThat(RECURRENCE.setGroupId(GROUP_ID), sameInstance(RECURRENCE));
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
    public void testSetPrevEventGet() {
        final Id newId = Id.generateRandom();

        assertThrows(NullPointerException.class, () -> RECURRENCE.setPrevEvent(null));
        Recurrence recurrChanged = RECURRENCE.setPrevEvent(Optional.of(newId));
        assertEquals(newId, recurrChanged.getPrevEvent().orElse(null));
        recurrChanged = RECURRENCE.setPrevEvent(Optional.ofNullable(null));
        assertEquals(Optional.empty(), recurrChanged.getPrevEvent());
        assertEquals(PREV_EVENT_ID, RECURRENCE.getPrevEvent().orElse(null));
        assertThat(RECURRENCE.setPrevEvent(Optional.of(PREV_EVENT_ID)), sameInstance(RECURRENCE));
    }

    @Test
    public void testSetNextEventGet() {
        final Id newId = Id.generateRandom();

        assertThrows(NullPointerException.class, () -> RECURRENCE.setNextEvent(null));
        Recurrence recurrChanged = RECURRENCE.setNextEvent(Optional.of(newId));
        assertEquals(newId, recurrChanged.getNextEvent().orElse(null));
        recurrChanged = RECURRENCE.setNextEvent(Optional.ofNullable(null));
        assertEquals(Optional.empty(), recurrChanged.getNextEvent());
        assertEquals(NEXT_EVENT_ID, RECURRENCE.getNextEvent().orElse(null));
        assertThat(RECURRENCE.setNextEvent(Optional.of(NEXT_EVENT_ID)), sameInstance(RECURRENCE));
    }

    @Test
    public void toStringContainsAllFields() {
        String str = RECURRENCE.toString();
        assertThat(str, containsString(GROUP_ID.toString()));
        assertThat(str, containsString(DURATION_WEEK.toString()));
        assertThat(str, containsString(PREV_EVENT_ID.toString()));
        assertThat(str, containsString(NEXT_EVENT_ID.toString()));
    }

    @Test
    public void equalsBehavesAsExpected() {
        Recurrence recurr1 = new Recurrence(GROUP_ID, DURATION_WEEK, Optional.of(PREV_EVENT_ID), Optional.of(NEXT_EVENT_ID));
        assertThat(RECURRENCE, is(RECURRENCE));
        assertThat(RECURRENCE, is(recurr1));
        Recurrence recurr2 = new Recurrence(Id.generateRandom(), DURATION_WEEK, Optional.of(PREV_EVENT_ID), Optional.of(NEXT_EVENT_ID));
        assertThat(RECURRENCE, not(is(recurr2)));
        Recurrence recurr3 = new Recurrence(GROUP_ID, SECONDS_WEEK, Optional.of(PREV_EVENT_ID), Optional.of(NEXT_EVENT_ID));
        assertThat(RECURRENCE, is(recurr3));
        Recurrence recurr4 = new Recurrence(GROUP_ID, DURATION_WEEK.minusMinutes(4), Optional.of(PREV_EVENT_ID), Optional.of(NEXT_EVENT_ID));
        assertThat(RECURRENCE, not(is(recurr4)));
        Recurrence recurr5 = new Recurrence(GROUP_ID, DURATION_WEEK, Optional.of(Id.generateRandom()), Optional.of(NEXT_EVENT_ID));
        Recurrence recurr6 = new Recurrence(GROUP_ID, DURATION_WEEK, Optional.of(PREV_EVENT_ID), Optional.of(Id.generateRandom()));
        Recurrence recurr7 = new Recurrence(GROUP_ID, DURATION_WEEK, Optional.ofNullable(null), Optional.of(NEXT_EVENT_ID));
        Recurrence recurr8 = new Recurrence(GROUP_ID, DURATION_WEEK, Optional.of(PREV_EVENT_ID), Optional.ofNullable(null));
        assertThat(RECURRENCE, not(is(recurr5)));
        assertThat(RECURRENCE, not(is(recurr6)));
        assertThat(RECURRENCE, not(is(recurr7)));
        assertThat(RECURRENCE, not(is(recurr8)));
    }

    @Test
    public void hashCodeIsEqualCompatible(){
        Recurrence event = new Recurrence(GROUP_ID, DURATION_WEEK, Optional.of(PREV_EVENT_ID), Optional.of(NEXT_EVENT_ID));
        assertThat(event, is(RECURRENCE));
        assertThat(event, not(sameInstance(RECURRENCE)));
        assertThat(event.hashCode(), is(RECURRENCE.hashCode()));
    }
}
