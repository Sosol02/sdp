package com.github.onedirection.navigation;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.github.onedirection.R;
import com.github.onedirection.database.Database;
import com.github.onedirection.events.EventCreator;
import com.github.onedirection.navigation.fragment.calendar.CalendarFragment;
import com.github.onedirection.navigation.fragment.calendar.CustomCalendarView;
import com.github.onedirection.navigation.fragment.map.MapFragment;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.onedirection.testhelpers.FirstMatch.firstMatch;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class CalendarUITest {

    private CountingIdlingResource idling;
    private CalendarFragment fragment;

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

    @Before
    public void setupForTest() throws InterruptedException {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_calendar)).perform(ViewActions.click());

        testRule.getScenario().onActivity(activity -> {
            fragment = (CalendarFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment).getChildFragmentManager().getPrimaryNavigationFragment();
            idling = fragment.getIdlingResource();
            IdlingRegistry.getInstance().register(idling);
        });
    };

    @Test
    public void changeMonthTest(){
        ViewInteraction currentMonth =  onView(allOf(withId(R.id.currentDate)));

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.nextBtn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.custom_calendar_view),
                                        0),
                                2),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction newMonth =  onView(allOf(withId(R.id.currentDate)));
        assertThat(currentMonth, not(newMonth));
    }

    @Test
    public void testAddEvent(){
        DataInteraction date = onData(anything())
                .inAdapterView(allOf(withId(R.id.gridView),
                        childAtPosition(
                                withId(R.id.custom_calendar_view),
                                2)))
                .atPosition(9);
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

        ViewInteraction materialButton2 = onView(allOf(withId(R.id.buttonEventAdd)));
        materialButton2.perform(scrollTo(), click());

        assertThat(date.onChildView(allOf(withId(R.id.events_id))).toString(), not(""));
    }

    @Test
    public void testViewEvents() throws InterruptedException {
        DataInteraction date = onData(anything())
                .inAdapterView(allOf(withId(R.id.gridView),
                        childAtPosition(
                                withId(R.id.custom_calendar_view),
                                2)))
                .atPosition(9);
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

        ViewInteraction materialButton2 = onView(allOf(withId(R.id.buttonEventAdd)));
        materialButton2.perform(scrollTo(), click());

        date.perform(click());


        ViewInteraction viewEventsButton = onView(
                allOf(withId(R.id.viewEvents), withText(R.string.view_events),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.custom),
                                        0),
                                1),
                        isDisplayed()));
        viewEventsButton.perform(click());

        onView(withId(R.id.dayEventsList)).check(matches(isDisplayed()));

        ViewInteraction materialButton4 = onView(
                firstMatch(withId(R.id.eventDeleteButton)));
        materialButton4.perform(click());
    }






    @After
    public void AtEndTest(){
        IdlingRegistry.getInstance().unregister(idling);
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
