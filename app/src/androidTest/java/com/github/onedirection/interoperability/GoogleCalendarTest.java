package com.github.onedirection.interoperability;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.event.model.Recurrence;
import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.interoperability.gcalendar.ExportFragment;
import com.github.onedirection.interoperability.gcalendar.GoogleCalendar;
import com.github.onedirection.utils.Id;
import com.google.api.services.calendar.model.Event;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

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
}
