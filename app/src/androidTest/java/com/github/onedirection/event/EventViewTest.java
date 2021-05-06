package com.github.onedirection.event;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.github.onedirection.R;
import com.github.onedirection.eventviewer.EventView;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.utils.Id;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class EventViewTest {


    private final static Id ID = Id.generateRandom();
    private final static String NAME = "Event nameZZZ";
    private final static String LOCATION_NAME = "Location name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, LOCATION_NAME);
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().plusDays(1);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plusDays(2);

    private final static String EPFL_QUERY = "EPFL";
    private final static String EPFL_CANTON = "Vaud";

    private final static Event event = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);

    @Rule
    public ActivityTestRule<NavigationActivity> mActivityTestRule = new ActivityTestRule<>(NavigationActivity.class);


    @Test

    public void testActivityWithIntent(){
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), EventView.class);

        ArrayList<Event> events = new ArrayList<Event>();
        events.add(event);
        Intent intent = EventView.putEventListExtra(i,events);
        ActivityScenario.launch(intent).onActivity(a -> {});
        onView(ViewMatchers.withId(R.id.eventName)).check(matches(withText(containsString("Event nameZZZ"))));
    }


    @Test
    public void testUiEventViewer() {


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
                allOf(withId(R.id.nav_create_event),
                        childAtPosition(
                                allOf(withId(R.id.design_navigation_view),
                                        childAtPosition(
                                                withId(R.id.nav_view),
                                                0)),
                                5),
                        isDisplayed()));
        navigationMenuItemView.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editEventName),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0),
                                2)));
        appCompatEditText.perform(scrollTo(), replaceText("jj"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editEventName), withText("jj"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0),
                                2)));
        appCompatEditText2.perform(pressImeActionButton());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.editEventLocationName),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0),
                                4)));
        appCompatEditText3.perform(pressImeActionButton());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.buttonEventAdd), withText("Create event"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0),
                                12)));
        materialButton.perform(scrollTo(), click());

        ViewInteraction navigationMenuItemView2 = onView(
                allOf(withId(R.id.nav_event_viewer),
                        childAtPosition(
                                allOf(withId(R.id.design_navigation_view),
                                        childAtPosition(
                                                withId(R.id.nav_view),
                                                0)),
                                7),
                        isDisplayed()));
        navigationMenuItemView2.perform(click());

        ViewInteraction eventIsDisplayed = onView(
                allOf(withId(R.id.recyclerEventView),
                        isDisplayed()));
    }



    @Test
    public void eventViewerTest(){

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

        onView(isDisplayed());
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
