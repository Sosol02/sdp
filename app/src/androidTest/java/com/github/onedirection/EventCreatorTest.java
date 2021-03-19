package com.github.onedirection;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.geocoding.NamedCoordinates;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.navigation.fragment.calendar.CalendarFragment;
import com.github.onedirection.utils.Id;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)

public class EventCreatorTest {

    private final static Id ID = Id.generateRandom();
    private final static String NAME = "Event name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, "Location name");
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
    private final static Duration DURATION = Duration.of(1, ChronoUnit.HOURS);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plus(DURATION).truncatedTo(Event.TIME_PRECISION);

    private final static Event EVENT = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);

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

    public void gotoCreator(){
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
    public void eventCreatorDisplaysEventToUpdate(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreator.class);
        EventCreator.putEventExtra(intent, EVENT);

        try(ActivityScenario<EventCreator> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.editEventName)).check(ViewAssertions.matches(ViewMatchers.withText(NAME)));
            onView(withId(R.id.editEventLocation)).check(ViewAssertions.matches(ViewMatchers.withText(LOCATION.name)));
        }
    }
}

