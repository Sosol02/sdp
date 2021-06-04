package com.github.onedirection.navigation;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.github.onedirection.R;
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.model.Recurrence;
import com.github.onedirection.geolocation.model.NamedCoordinates;
import com.github.onedirection.navigation.fragment.home.DisplayEvent;
import com.github.onedirection.utils.Id;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
public class DisplayEventTest {

    private final static Id ID = Id.generateRandom();
    private final static String NAME = "shrek";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, "Location name");
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION).plusMinutes(2);
    private final static Duration DURATION = Duration.of(1, ChronoUnit.HOURS);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plus(DURATION).truncatedTo(Event.TIME_PRECISION);
    private final static Recurrence RECURRING_PERIOD = new Recurrence(Id.generateRandom(), Duration.ofDays(1), END_TIME); //Daily
    private final static Event EVENT = new Event(ID, NAME, LOCATION, START_TIME, END_TIME,false);

    @Rule
    public ActivityScenarioRule<NavigationActivity> mActivityTestRule = new ActivityScenarioRule<>(NavigationActivity.class);

    @Test
    public void displayEventTest() {
        ActivityScenarioRule<NavigationActivity> activity = new ActivityScenarioRule<>(NavigationActivity.class);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_create_event)).perform(ViewActions.click());
        onView(withId(R.id.editEventName)).perform(ViewActions.click());
        onView(withId(R.id.editEventName)).perform(ViewActions.typeText("event yeah"));
        pressBack();
        onView(withId(R.id.buttonEventAdd)).perform(ViewActions.click());
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_home)).perform(ViewActions.click());
        onView(withId(R.id.recyclerEventView)).perform(ViewActions.longClick());
    }
    @Test
    public void displayWorksCorrectlyPressButton1() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DisplayEvent.class);
        DisplayEvent.putEventExtra(intent, EVENT);
        ActivityScenario.launch(intent).onActivity( a->{
            DisplayEvent activity = (DisplayEvent) a;
        });
        onView(withId(R.id.eventNameDisplay)).check(matches(isDisplayed()));
        onView(withId(R.id.eventStartTimeDisplay)).check(matches(isDisplayed()));
        onView(withId(R.id.eventEndTimeDisplay)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonDisplayDelete)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonDisplay)).check(matches(isDisplayed()));
    }

    @Test
    public void displayWorksCorrectlyPressButton2() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DisplayEvent.class);
        DisplayEvent.putEventExtra(intent, EVENT);
        ActivityScenario.launch(intent).onActivity( a->{
            DisplayEvent activity = (DisplayEvent) a;
        });
        onView(withId(R.id.buttonDisplayDelete)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonDisplay)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonDisplayDelete)).perform(ViewActions.click());
    }

    @Test
    public void displayWorksCorrectlyForFavorite() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DisplayEvent.class);
        Event e = new Event(ID, NAME, LOCATION, START_TIME, END_TIME, RECURRING_PERIOD,false);

        DisplayEvent.putEventExtra(intent, e);
        ActivityScenario.launch(intent).onActivity( a->{
            DisplayEvent activity = (DisplayEvent) a;
        });
    }

    @Test
    public void eventCreatorCanBeOpened() {
        ActivityScenarioRule<NavigationActivity> activity = new ActivityScenarioRule<>(NavigationActivity.class);
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.buttonEventAdd)).check(matches(isDisplayed()));
    }

    @Test
    public void testAllFunctionalitiesWork() throws ExecutionException,InterruptedException {
        new EventQueries(Database.getDefaultInstance()).addNonRecurringEvent(EVENT).get();
        ActivityScenarioRule<NavigationActivity> activity = new ActivityScenarioRule<>(NavigationActivity.class);
        onView(withId(R.id.recyclerEventView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.favorite_button)).perform(click());
        onView(withId(R.id.favorite_button)).perform(click());
        onView(withId(R.id.favorite_button)).perform(click());
        onView(withId(R.id.buttonDisplay)).perform(click());
        onView(withId(R.id.editEventName)).perform(ViewActions.click());
        onView(withId(R.id.editEventName)).perform(ViewActions.typeText("shrek"));
        pressBack();
        onView(withId(R.id.buttonEventAdd)).perform(ViewActions.click());
        onView(withId(R.id.recyclerEventView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        pressBack();
    }
}
