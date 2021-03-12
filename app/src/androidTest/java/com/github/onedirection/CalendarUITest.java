package com.github.onedirection;

import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.navigation.fragment.calendar.CalendarFragment;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;


@RunWith(AndroidJUnit4.class)

public class CalendarUITest {


        @Rule
        public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

        @Test
        public void calendarShouldAppear(){
            onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
            onView(withId(R.id.nav_calendar)).perform(ViewActions.click());

            onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                    .check(matches(withText(R.string.menu_calendar)));
            onView(withIndex(withId(R.id.calendarView), 0)).check(matches(isDisplayed()));
        }

        public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
            return new TypeSafeMatcher<View>() {
                int currentIndex = 0;

                @Override
                public void describeTo(Description description) {
                    description.appendText("with index: ");
                    description.appendValue(index);
                    matcher.describeTo(description);
                }

                @Override
                public boolean matchesSafely(View view) {
                    return matcher.matches(view) && currentIndex++ == index;
                }
            };
        }
}
