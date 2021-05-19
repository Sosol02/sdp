package com.github.onedirection.navigation;

import android.widget.TextView;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.navigation.NavigationActivity;

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
public class NavigationActivityTest {

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

    @Test
    public void testOpenableDrawer() {
        onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationBetweenFragments() {

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_calendar)).perform(ViewActions.click());

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_calendar)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_home)).perform(ViewActions.click());

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_home)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_map)).perform(ViewActions.click());

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_map)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_sign)).perform(ViewActions.click());

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_sign)));
    }
}
