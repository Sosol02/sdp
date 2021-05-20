package com.github.onedirection.interoperability;

import android.util.Log;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.event.Recurrence;
import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.interoperability.gcalendar.ExportFragment;
import com.github.onedirection.interoperability.gcalendar.GoogleCalendar;
import com.github.onedirection.utils.Id;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class GoogleCalendarTest {

    @Test
    public void exportButtonCanBeClicked() {
        FragmentScenario<ExportFragment> fragment = FragmentScenario.launchInContainer(ExportFragment.class);
        onView(withId(R.id.buttonGCalendarExport)).check(matches(isClickable()));
        Intents.init();
        assertThat(Intents.getIntents().isEmpty(), is(true));

        onView(withId(R.id.buttonGCalendarExport)).perform(click());

        assertThat(Intents.getIntents().isEmpty(), is(false));

        Intents.release();
    }

    @Test
    public void toGCalendarEventConvertsStartEndProperly() {
        Id id = Id.generateRandom();
        ZonedDateTime startTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        ZonedDateTime endTime = startTime.plusHours(1);
        com.github.onedirection.event.Event e =
                new com.github.onedirection.event.Event(id, "EVENT", "LOCATION", startTime, endTime);

        Event gcEvent = GoogleCalendar.toGCalendarEvents(e);

        assertEquals(startTime.toEpochSecond(), gcEvent.getStart().getDateTime().getValue()/1000);
        assertEquals(endTime.toEpochSecond(), gcEvent.getEnd().getDateTime().getValue()/1000);
    }

    @Test
    public void toGCalendarEventConvertsNameAndIdAndLocationName() {
        Id id = Id.generateRandom();
        String name = "EVENT";
        String locName = "LOCATION";
        ZonedDateTime startTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        ZonedDateTime endTime = startTime.plusHours(1);
        com.github.onedirection.event.Event e =
                new com.github.onedirection.event.Event(id, name, locName, new Coordinates(0, 0), startTime, endTime);

        Event gcEvent = GoogleCalendar.toGCalendarEvents(e);

        assertEquals(id.getUuid(), gcEvent.getId());
        assertEquals(name, gcEvent.getSummary());
        assertEquals(locName, gcEvent.getLocation());
    }

    @Test
    public void toGCalendarEventConvertsRecurrenceFirstOccurrence() {
        Id id = Id.generateRandom();
        String name = "EVENT";
        String locName = "LOCATION";
        ZonedDateTime startTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        ZonedDateTime endTime = startTime.plusHours(1);
        Recurrence r = new Recurrence(id, ChronoUnit.WEEKS.getDuration(), startTime.plusWeeks(3));
        com.github.onedirection.event.Event e =
                new com.github.onedirection.event.Event(id, name, locName, startTime, endTime, r);

        Event gcEvent = GoogleCalendar.toGCalendarEvents(e);
        String[] recurrence = gcEvent.getRecurrence().get(0).split(";");

        assertEquals(id.getUuid(), gcEvent.getRecurringEventId());
        assertEquals("WEEKLY", recurrence[0].substring(11));
        assertEquals("4", recurrence[1].substring(6));
    }

    @Test
    public void fromGCalendarEventsBasicTests() {
        Id id = Id.generateRandom();
        String name = "EVENT";
        String locName = "LOCATION";
        ZonedDateTime startTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        ZonedDateTime endTime = startTime.plusHours(1);
        Recurrence r = new Recurrence(id, ChronoUnit.WEEKS.getDuration(), startTime.plusWeeks(3));
        com.github.onedirection.event.Event e =
                new com.github.onedirection.event.Event(id, name, locName, startTime, endTime, r);

        Event gcEvent = GoogleCalendar.toGCalendarEvents(e);
        com.github.onedirection.event.Event event = GoogleCalendar.fromGCalendarEvents(gcEvent);

        assertEquals(event.getId(), event.getRecurrence().get().getGroupId());
        assertEquals(name, event.getName());
        assertEquals("", event.getLocationName());
        assertEquals(startTime, event.getStartTime());
        assertEquals(endTime, event.getEndTime());
        assertEquals(ChronoUnit.WEEKS.getDuration(), event.getRecurrence().get().getPeriod());
        assertEquals(startTime.plusWeeks(3), event.getRecurrence().get().getEndTime());
    }
}
