package com.github.onedirection;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.navigation.fragment.calendar.CalendarFragment;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)

public class EventCreatorTest {


    @Rule
    public ActivityScenarioRule<EventCreator> eventCreator = new ActivityScenarioRule<>(EventCreator.class);

    @Before
    public void setUp() {
        Intents.init();
    }

     @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void verifyEventActivityIsCorrectlyCreated() {
        //Intents.init();
        //onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        //onView(withId(R.id.nav_calendar)).perform(ViewActions.click());
        //String name = onView(withId(R.id.editTextName)).check();
        //String location = onView(withId(R.id.editTextLocation)).toString();
        //String date = onView(withId(R.id.editTextDate)).toString();
        //String start_time = onView(withId(R.id.editTextStartTime)).toString();
        //String end_time = onView(withId(R.id.editTextEndTime)).toString();

        onView(withId(R.id.buttonEventAdd)).perform(ViewActions.click());

        intended(hasComponent(EventsView.class.getName()));

        //String name_view = onView(withId(R.id.textViewNameView)).toString().toString();
        //String location_view = onView(withId(R.id.textViewLocationView)).toString();
        //String date_view = onView(withId(R.id.textViewDateView)).toString();
        //String start_time_view = onView(withId(R.id.textViewStartTimeView)).toString();
        //String end_time_view = onView(withId(R.id.textViewEndTimeView)).toString();

        //assertThat(name,is(name_view));
        //assertEquals(location,location_view);
        //assertEquals(date,date_view);
        //assertEquals(start_time,start_time_view);
        //assertEquals(end_time,end_time_view);
        //Intents.release();


        //onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
        //       .check(matches(withText(R.string.menu_calendar)));
        //onView(withIndex(withId(R.id.calendarView), 0)).check(matches(isDisplayed()));
    }
}

