package com.github.onedirection;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.events.Event;
import com.github.onedirection.events.Recurrence;
import com.github.onedirection.eventviewer.EventViewerAdapter;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.utils.Id;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class EventViewTest {
    @Rule
    public ActivityScenarioRule<NavigationActivity> mActivityTestRule = new ActivityScenarioRule<NavigationActivity>(NavigationActivity.class);
    List<Event> events = new ArrayList<Event>();
    RecyclerView eventList;
    EventViewerAdapter eventViewerAdapter;


    @Test
    public void viewingEventsWork() {

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction navigationMenuItemView = onView(
                allOf(withId(R.id.nav_calendar),
                        childAtPosition(
                                allOf(withId(R.id.design_navigation_view),
                                        childAtPosition(
                                                withId(R.id.nav_view),
                                                0)),
                                2),
                        isDisplayed()));
        navigationMenuItemView.perform(click());
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DataInteraction date = onData(anything())
                .inAdapterView(allOf(withId(R.id.gridView),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2)))
                .atPosition(15);
        date.perform(click());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.addEvent), withText("Add Event"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.custom),
                                        0),
                                0),
                        isDisplayed()));
        materialButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editEventName),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0),
                                2)));
        appCompatEditText.perform(scrollTo(), replaceText("Donkey"), closeSoftKeyboard());

        ViewInteraction materialButton2 = onView(
                allOf(withId(R.id.buttonEventAdd), withText("Create event"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0),
                                10)));
        materialButton2.perform(scrollTo(), click());
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction navigationMenuItemView2 = onView(
                withId(R.id.nav_event_viewer));
        navigationMenuItemView.perform(click());

        ViewInteraction checkHasEvent = onView(
                allOf(withContentDescription("check has event"),
                        childAtPosition(
                                allOf(withId(R.id.recyclerEventView),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        ViewInteraction eventIsDisplayed = onView(
                allOf(withId(R.id.recyclerEventView),
                        isDisplayed()));


        ConcreteDatabase database = ConcreteDatabase.getDatabase();
        EventQueries queryManager = new EventQueries(database);
        ZonedDateTime firstInstantOfMonth = ZonedDateTime.of(2021, 4, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        CompletableFuture<List<Event>> monthEventsFuture = queryManager.getEventsByMonth(firstInstantOfMonth);
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            Id ID = Id.generateRandom();
            String NAME = "Event name";
            NamedCoordinates LOCATION = new NamedCoordinates(0, 0, "Location name");
            ZonedDateTime START_TIME = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
            Duration DURATION = Duration.of(1, ChronoUnit.HOURS);
            ZonedDateTime END_TIME = ZonedDateTime.now().plus(DURATION).truncatedTo(Event.TIME_PRECISION);
            Recurrence RECURRING_PERIOD = new Recurrence(Id.generateRandom(), Duration.ofDays(1), END_TIME); //Daily

            Event e = new Event(ID, NAME, LOCATION, START_TIME, END_TIME, RECURRING_PERIOD);
            events.add(e);
            eventViewerAdapter = new EventViewerAdapter(events);
            eventList.setAdapter(eventViewerAdapter);
        });
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
    @Test
    public void viewingEventsWorkNoUi() {
        ConcreteDatabase database = ConcreteDatabase.getDatabase();
        EventQueries queryManager = new EventQueries(database);
        ZonedDateTime firstInstantOfMonth = ZonedDateTime.of(2021, 4, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        CompletableFuture<List<Event>> monthEventsFuture = queryManager.getEventsByMonth(firstInstantOfMonth);
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            Id ID = Id.generateRandom();
            String NAME = "Event name";
            NamedCoordinates LOCATION = new NamedCoordinates(0, 0, "Location name");
            ZonedDateTime START_TIME = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
            Duration DURATION = Duration.of(1, ChronoUnit.HOURS);
            ZonedDateTime END_TIME = ZonedDateTime.now().plus(DURATION).truncatedTo(Event.TIME_PRECISION);
            Recurrence RECURRING_PERIOD = new Recurrence(Id.generateRandom(), Duration.ofDays(1), END_TIME); //Daily

            Event e = new Event(ID, NAME, LOCATION, START_TIME, END_TIME, RECURRING_PERIOD);
            events.add(e);
            eventViewerAdapter = new EventViewerAdapter(events);
            eventList.setAdapter(eventViewerAdapter);
        });
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction navigationMenuItemView = onView(
                withId(R.id.nav_event_viewer));
        navigationMenuItemView.perform(click());

        ViewInteraction checkHasEvent = onView(
                allOf(withContentDescription("check has event"),
                        childAtPosition(
                                allOf(withId(R.id.recyclerEventView),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        ViewInteraction eventIsDisplayed = onView(
                allOf(withId(R.id.recyclerEventView),
                        isDisplayed()));
    }
}
