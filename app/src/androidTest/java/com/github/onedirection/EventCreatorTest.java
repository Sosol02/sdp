package com.github.onedirection;

import android.content.Intent;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.geocoding.NamedCoordinates;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.utils.Id;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZonedDateTime;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


@RunWith(AndroidJUnit4.class)

public class EventCreatorTest {

    private final static Id ID = Id.generateRandom();
    private final static String NAME = "Event name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, "Location name");
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().plusDays(1);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plusDays(2);

    private final static Event EVENT = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);
    private final static Event EVENT_BIS = new Event(
            ID,
            "Other name",
            new NamedCoordinates(1, 1, "Other location"),
            ZonedDateTime.now(),
            ZonedDateTime.now().plusHours(10)
    );

    @Rule
    public ActivityScenarioRule<NavigationActivity> eventCreator = new ActivityScenarioRule<>(NavigationActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    public void gotoCreator() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_create_event)).perform(ViewActions.click());
    }

    @Test
    public void verifyEventViewIsCorrectlyCalled() {
        gotoCreator();

        onView(withId(R.id.buttonEventAdd)).perform(ViewActions.click());

        intended(allOf(
                hasComponent(EventsView.class.getName()),
                hasExtra(is(EventCreator.EXTRA_EVENT), is(instanceOf(Event.class)))
        ));
    }

    @Test
    public void eventCreatorDisplaysEventToUpdate() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreator.class);
        EventCreator.putEventExtra(intent, EVENT);

        try (ActivityScenario<EventCreator> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.editEventName)).check(ViewAssertions.matches(ViewMatchers.withText(NAME)));
            onView(withId(R.id.editEventLocation)).check(ViewAssertions.matches(ViewMatchers.withText(LOCATION.name)));
        }
    }

    private void setTimePicker(int startId, ZonedDateTime time) {
        onView(withId(startId)).perform(ViewActions.click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(time.getHour(), time.getMinute()));
        onView(withId(android.R.id.button1)).perform(ViewActions.click());
    }

    private void setDatePicker(int startId, ZonedDateTime date) {
        onView(withId(startId)).perform(ViewActions.click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth()));
        onView(withId(android.R.id.button1)).perform(ViewActions.click());
    }

    @Test
    public void eventSettingsCanBeChanged() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreator.class);
        EventCreator.putEventExtra(intent, EVENT_BIS);

        Intents.release();
        Intents.init();
        try (ActivityScenario<EventCreator> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.editEventName)).perform(ViewActions.clearText());
            onView(withId(R.id.editEventLocation)).perform(ViewActions.clearText());

            onView(withId(R.id.editEventName)).perform(ViewActions.typeText(NAME));
            ViewActions.closeSoftKeyboard();
            onView(withId(R.id.editEventLocation)).perform(ViewActions.typeText(LOCATION.name));
            ViewActions.closeSoftKeyboard();
            setTimePicker(R.id.buttonStartTime, START_TIME);
            setDatePicker(R.id.buttonStartDate, START_TIME);
            setTimePicker(R.id.buttonEndTime, END_TIME);
            setDatePicker(R.id.buttonEndDate, END_TIME);

            onView(withId(R.id.buttonEventAdd)).perform(ViewActions.click());

            intended(hasExtra(EventCreator.EXTRA_EVENT, EVENT));
        }
    }
}

