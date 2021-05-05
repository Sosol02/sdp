package com.github.onedirection.events;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;

import com.github.onedirection.EventsView;
import com.github.onedirection.R;
import com.github.onedirection.events.ui.EventCreator;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.utils.Id;

import org.junit.Test;

import java.time.ZonedDateTime;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class EventViewTest {


    private final static Id ID = Id.generateRandom();
    private final static String NAME = "Event name";
    private final static String LOCATION_NAME = "Location name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, LOCATION_NAME);
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().plusDays(1);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plusDays(2);

    private final static Event EVENT = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);

    @Test
    public void eventNameIsDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventsView.class);
        EventCreator.putEventExtra(intent, EVENT);

        try (ActivityScenario<EventsView> scenario = ActivityScenario.launch(intent)) {
            onView(ViewMatchers.withId(R.id.textViewNameView)).check(matches(withText(EVENT.getName())));
        }
    }
}
