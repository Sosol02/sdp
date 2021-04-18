package com.github.onedirection.navigation;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.github.onedirection.R;
import com.github.onedirection.navigation.NavigationActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CalendarUITest {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);

    @Rule
    public ActivityScenarioRule<NavigationActivity> mActivityTestRule = new ActivityScenarioRule<>(NavigationActivity.class);

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
    public void changingMonthsCorrectlyUpdatesCalendarTest() {
        String firstDate;
        String nextMonthDate;

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
        ViewInteraction textView = onView(
                allOf(withId(R.id.currentDate),
                        withParent(withParent(withId(R.id.activity_custom_calendar))),
                        isDisplayed()));
        textView.check(matches(isDisplayed()));
        firstDate = textView.toString();

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.nextBtn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.activity_custom_calendar),
                                        0),
                                2),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.currentDate),
                        withParent(withParent(withId(R.id.activity_custom_calendar))),
                        isDisplayed()));
        textView2.check(matches(isDisplayed()));
        nextMonthDate = textView2.toString();

        assertThat(firstDate, not(nextMonthDate));
    }
}
