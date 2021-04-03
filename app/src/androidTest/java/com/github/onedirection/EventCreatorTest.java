package com.github.onedirection;

import android.content.Intent;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.onedirection.events.Event;
import com.github.onedirection.events.EventCreator;
import com.github.onedirection.geocoding.Coordinates;
import com.github.onedirection.geocoding.NamedCoordinates;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;


@RunWith(AndroidJUnit4.class)
public class EventCreatorTest {

    private final static Id ID = Id.generateRandom();
    private final static String NAME = "Event name";
    private final static String LOCATION_NAME = "Location name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, LOCATION_NAME);
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().plusDays(1);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plusDays(2);

    private final static String EPFL_QUERY = "EPFL";
    private final static String EPFL_CANTON = "Vaud";

    private final static Event EVENT = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);
    private final static Event EVENT_BIS = new Event(
            ID,
            "Other name",
            "Other location",
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

    // Those do not work on cirrus...
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

    public void testIsMainFragment(){
        onView(withId(R.id.textEventCreatorTitle)).check(matches(withText(containsString("Create"))));
    }

    public void testIsGeolocationFragment(){
        onView(withId(R.id.textEventCreatorTitle)).check(matches(withText(containsString("location"))));
    }

    @Test
    public void verifyEventViewIsCorrectlyCalled() {
        onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), click());

        intended(allOf(
                hasComponent(EventsView.class.getName()),
                hasExtra(is(EventCreator.EXTRA_EVENT), is(instanceOf(Event.class)))
        ));
    }

    @Test
    public void geolocationTabIsOpenedWhenNoGeolocationIsSet(){
        onView(withId(R.id.checkGeolocation)).perform(scrollTo(), click());

        testIsGeolocationFragment();
        onView(withId(R.id.buttonSetGeolocation)).check(matches(not(isEnabled())));
    }

    @Test
    public void geolocationTabIsNotOpenedIfGeolocationAlreadySet(){
        onView(withId(R.id.checkGeolocation)).perform(scrollTo(), click());
        testIsGeolocationFragment();

        onView(withId(R.id.buttonUseCurrentLocation)).perform(scrollTo(), click());
        onView(withId(R.id.buttonSetGeolocation)).perform(scrollTo(), click());

        onView(withId(R.id.checkGeolocation)).perform(scrollTo(), click(), click());
        testIsMainFragment();
    }

    @Test
    public void geocodingCanBeUsed() {
        onView(withId(R.id.checkGeolocation)).perform(scrollTo(), click());
        testIsGeolocationFragment();

        onView(withId(R.id.editLocationQuery)).perform(scrollTo(), clearText(), typeText(EPFL_QUERY));
        closeSoftKeyboard();
        onView(withId(R.id.buttonSearchLocation)).perform(scrollTo(), click());
        onView(withId(R.id.textSelectedLocationFull)).check(matches(withText(containsString(EPFL_CANTON))));
    }

    @Test
    public void phoneLocationCanBeUsed() {
        List<String> expected = List.of("lat", "lon");

        onView(withId(R.id.checkGeolocation)).perform(scrollTo(), click());
        testIsGeolocationFragment();

        onView(withId(R.id.buttonUseCurrentLocation)).perform(scrollTo(), click());
        onView(withId(R.id.textSelectedLocationFull)).check(matches(withText(stringContainsInOrder(expected))));
    }

    @Test
    public void eventDateCanBeSpecifiedAsInput() {
        final LocalDate date = LocalDate.of(1000, 10, 1);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreator.class);
        EventCreator.putDateExtra(intent, date);

        try (ActivityScenario<EventCreator> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.buttonStartDate)).check(matches(withText(date.toString())));
        }
    }

    @Test
    public void eventCanBeSpecifiedAsInput() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreator.class);
        EventCreator.putEventExtra(intent, EVENT);

        try (ActivityScenario<EventCreator> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.editEventName)).check(matches(withText(EVENT.getName())));

            onView(withId(R.id.checkGeolocation)).check(matches(isChecked()));

            onView(withId(R.id.buttonStartDate)).check(matches(withText(EVENT.getStartTime().toLocalDate().toString())));
            onView(withId(R.id.buttonStartTime)).check(matches(withText(EVENT.getStartTime().toLocalTime().toString())));

            onView(withId(R.id.buttonEndDate)).check(matches(withText(EVENT.getEndTime().toLocalDate().toString())));
            onView(withId(R.id.buttonEndTime)).check(matches(withText(EVENT.getEndTime().toLocalTime().toString())));

            onView(withId(R.id.buttonGotoGeolocation)).perform(scrollTo(), click());

            onView(withId(R.id.textSelectedLocationFull)).check(matches(withText(EVENT.getLocation().get().toString())));
        }
    }
}

