package com.github.onedirection;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.events.Event;
import com.github.onedirection.eventviewer.EventViewerAdapter;
import com.github.onedirection.navigation.NavigationActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
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


        ConcreteDatabase database = ConcreteDatabase.getDatabase();
        EventQueries queryManager = new EventQueries(database);
        ZonedDateTime firstInstantOfMonth = ZonedDateTime.of(2021, 4, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        CompletableFuture<List<Event>> monthEventsFuture = queryManager.getEventsByMonth(firstInstantOfMonth);
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            eventViewerAdapter = new EventViewerAdapter(events);
            eventList.setAdapter(eventViewerAdapter);
        });

        assertEquals(eventViewerAdapter.getItemCount(), events.size());
        //eventViewerAdapter.onBindViewHolder( new EventViewerAdapter.ViewHolder(this),0);
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

}
