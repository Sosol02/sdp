package com.github.onedirection.navigation;



import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.github.onedirection.R;
import com.github.onedirection.navigation.fragment.home.HomeFragment;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;


@RunWith(AndroidJUnit4.class)
public class DisplayEventTest {

    @Rule
    public ActivityTestRule<NavigationActivity> mActivityTestRule = new ActivityTestRule<>(NavigationActivity.class);

    @Test
    public void displayEventTest() {
        ActivityScenarioRule<NavigationActivity> activity = new ActivityScenarioRule<>(NavigationActivity.class);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_create_event)).perform(ViewActions.click());
        onView(withId(R.id.editEventName)).perform(ViewActions.click());
        onView(withId(R.id.editEventName)).perform(ViewActions.typeText("event yeah"));
        pressBack();
        onView(withId(R.id.buttonEventAdd)).perform(ViewActions.click());
        pressBack();
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_home)).perform(ViewActions.click());
        onView(withId(R.id.recyclerEventView)).perform(ViewActions.longClick());
    }

}
