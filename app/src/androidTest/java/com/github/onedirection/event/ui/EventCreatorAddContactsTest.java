package com.github.onedirection.event.ui;


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
import androidx.test.filters.LargeTest;

import com.github.onedirection.R;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.navigation.fragment.calendar.CalendarFragment;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

@LargeTest

@RunWith(AndroidJUnit4.class)
public class EventCreatorAddContactsTest {

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);
    private CountingIdlingResource idling;
    private CalendarFragment fragment;

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
    public void eventCreatorAddContactsTest() {
        ViewInteraction appCompatImageButton = onView(allOf(withContentDescription("Open navigation drawer"), isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction navigationMenuItemView = onView(allOf(withId(R.id.nav_calendar), isDisplayed()));
        navigationMenuItemView.perform(click());

        DataInteraction linearLayout = onData(anything()).inAdapterView(allOf(withId(R.id.gridView)));
        linearLayout.perform(click());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.addEvent), withText("Add Event"), isDisplayed()));
        materialButton.perform(click());

        ViewInteraction appCompatEditText = onView(allOf(withId(R.id.contactInput),isDisplayed()));
        appCompatEditText.perform(replaceText("shrek@swamp.com"), closeSoftKeyboard());

        ViewInteraction materialButton2 = onView(allOf(withId(R.id.addContactButton), withText("Add"), isDisplayed()));
        materialButton2.perform(click());
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
