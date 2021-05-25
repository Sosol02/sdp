package com.github.onedirection.navigation;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.navigation.fragment.calendar.CalendarFragment;

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
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.onedirection.testhelpers.FirstMatch.firstMatch;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class CalendarUITest {

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);
    private CountingIdlingResource idling;
    private CalendarFragment fragment;

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
    }

    @Test
    public void changeMonthTest() {
        ViewInteraction currentMonth = onView(allOf(withId(R.id.currentDate)));

        ViewInteraction nextBtn = onView(allOf(withId(R.id.nextBtn)));
        nextBtn.perform(click());

        ViewInteraction newMonth = onView(allOf(withId(R.id.currentDate)));
        assertThat(currentMonth, not(newMonth));
    }

    @Test
    public void testAddEvent() {
        DataInteraction date = onData(anything()).inAdapterView(allOf(withId(R.id.gridView))).atPosition(14);
        date.perform(click());

        ViewInteraction eventName = onView(allOf(withId(R.id.editEventName), isDisplayed()));
        eventName.perform(replaceText("Shrek is love"), ViewActions.closeSoftKeyboard());

        ViewInteraction eventCreatorCreateBtn = onView(allOf(withId(R.id.buttonEventAdd)));
        eventCreatorCreateBtn.perform(scrollTo(), click());

        assertThat(date.onChildView(allOf(withId(R.id.nb_events))).toString(), not(""));
    }

    @Test
    public void testDeleteEvent() {
        DataInteraction date = onData(anything()).inAdapterView(allOf(withId(R.id.gridView))).atPosition(15);
        String nbEvents = date.onChildView(allOf(withId(R.id.nb_events))).toString();
        date.perform(click());

        ViewInteraction eventName = onView(allOf(withId(R.id.editEventName), isDisplayed()));
        eventName.perform(replaceText("Shrek is life"), ViewActions.closeSoftKeyboard());

        ViewInteraction eventCreatorCreateBtn = onView(allOf(withId(R.id.buttonEventAdd)));
        eventCreatorCreateBtn.perform(scrollTo(), click());

        date.perform(click());

        ViewInteraction viewEventsButton = onView(allOf(withId(R.id.viewEvents), withText(R.string.view_events), isDisplayed()));
        viewEventsButton.perform(click());

        onView(withId(R.id.dayEventsList)).check(matches(isDisplayed()));

        ViewInteraction deleteBtn = onView(allOf(withId(R.id.eventDeleteButton)));
        deleteBtn.perform(click());

        assertThat(date.onChildView(allOf(withId(R.id.nb_events))).toString(), is(nbEvents));
    }

    @Test
    public void testViewEvents() throws InterruptedException {
        DataInteraction date = onData(anything()).inAdapterView(allOf(withId(R.id.gridView))).atPosition(16);
        date.perform(click());

        ViewInteraction eventName = onView(allOf(withId(R.id.editEventName), isDisplayed()));
        eventName.perform(replaceText("It's all ogre now"), ViewActions.closeSoftKeyboard());

        ViewInteraction eventCreatorCreateBtn = onView(allOf(withId(R.id.buttonEventAdd)));
        eventCreatorCreateBtn.perform(scrollTo(), click());

        date.perform(click());

        ViewInteraction viewEventsButton = onView(allOf(withId(R.id.viewEvents), withText(R.string.view_events), isDisplayed()));
        viewEventsButton.perform(click());

        onView(withId(R.id.dayEventsList)).check(matches(isDisplayed()));
    }



    @After
    public void AtEndTest() {
        IdlingRegistry.getInstance().unregister(idling);
    }
}
