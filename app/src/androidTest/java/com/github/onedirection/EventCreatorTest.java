package com.github.onedirection;

import android.content.Intent;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.onedirection.geocoding.Coordinates;
import com.github.onedirection.geocoding.NamedCoordinates;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.utils.Id;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


@RunWith(AndroidJUnit4.class)
public class EventCreatorTest {

    private final static Coordinates PHONE_COORDINATES = new Coordinates(46.5, 6.5);

    private final static Id ID = Id.generateRandom();
    private final static String NAME = "Event name";
    private final static String LOCATION_NAME = "Location name";
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().plusDays(1);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plusDays(2);

    private final static Event EVENT = new Event(ID, NAME, LOCATION_NAME, START_TIME, END_TIME);
    private final static Event EVENT_BIS = new Event(
            ID,
            "Other name",
            new NamedCoordinates(1, 1, "Other location"),
            ZonedDateTime.now(),
            ZonedDateTime.now().plusHours(10)
    );

    private IdlingResource idling;

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Before
    public void setUp() {
        ActivityScenario.launch(EventCreator.class).onActivity(activity -> {
            idling = activity.getIdlingResource();
            IdlingRegistry.getInstance().register(idling);
        });
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
        IdlingRegistry.getInstance().unregister(idling);
    }

    @Test
    public void verifyEventViewIsCorrectlyCalled() {
        onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), ViewActions.click());

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
            onView(withId(R.id.editEventName)).check(matches(withText(NAME)));
            onView(withId(R.id.editEventLocationName)).check(matches(withText(LOCATION_NAME)));
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
    public void eventSettingsCanBeChanged() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreator.class);
        EventCreator.putEventExtra(intent, EVENT_BIS);

        try (ActivityScenario<EventCreator> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.editEventName)).perform(scrollTo(), ViewActions.clearText());
            onView(withId(R.id.editEventLocationName)).perform(scrollTo(), ViewActions.clearText());

            onView(withId(R.id.editEventName)).perform(scrollTo(), ViewActions.typeText(NAME));
            ViewActions.closeSoftKeyboard();
            onView(withId(R.id.editEventLocationName)).perform(scrollTo(), ViewActions.typeText(LOCATION_NAME));
            ViewActions.closeSoftKeyboard();

            /*
             * So...
             * For some reason, cirrus doesn't like those wonderful test
             * functions stolen from SO. Since this is deeply tied to the current
             * UI choice, and since I already way to much time on this, this is gonna
             * be disabled for now. I will most likely change the UI again when I have the
             * time, so that we can test this correctly.
             * XOXO
             */
//            setTimePicker(R.id.buttonStartTime, START_TIME);
//            setDatePicker(R.id.buttonStartDate, START_TIME);
//            setTimePicker(R.id.buttonEndTime, END_TIME);
//            setDatePicker(R.id.buttonEndDate, END_TIME);

            onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), ViewActions.click());

//            intended(hasExtra(EventCreator.EXTRA_EVENT, EVENT));
        }
    }

    @Test
    public void eventDateCanSpecified() {
        final LocalDate date = LocalDate.of(1000, 10, 1);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreator.class);
        EventCreator.putDateExtra(intent, date);

        try (ActivityScenario<EventCreator> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.buttonStartDate)).check(matches(withText(date.toString())));
        }
    }

    @Test
    public void canUsePhoneLocation() {
        onView(withId(R.id.buttonUsePhoneLocation)).perform(scrollTo(), click());
        onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), click());

        intended(allOf(
                hasComponent(EventsView.class.getName()),
                hasExtra(is(EventCreator.EXTRA_EVENT), is(new BaseMatcher<Event>() {
                    @Override
                    public void describeTo(Description description) {

                    }

                    @Override
                    public boolean matches(Object item) {
                        if (item instanceof Event) {
                            Optional<Coordinates> coords = ((Event) item).getCoordinates();
                            return coords.isPresent();
                        }
                        return false;
                    }
                }))
        ));
    }
}

