package com.github.onedirection.interoperability;

import android.content.Intent;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.intent.Intents;

import com.github.onedirection.R;
import com.github.onedirection.interoperability.gcalendar.ExportFragment;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GoogleCalendarTest {

    @Test
    public void exportButtonCanBeClicked() {
        FragmentScenario<ExportFragment> fragment = FragmentScenario.launchInContainer(ExportFragment.class);
        fragment.onFragment(exportFragment -> exportFragment.setExport(false));
        onView(withId(R.id.buttonGCalendarExport)).check(matches(isClickable()));
        Intents.init();
        assertThat(Intents.getIntents().isEmpty(), is(true));

        onView(withId(R.id.buttonGCalendarExport)).perform(click());

        assertThat(Intents.getIntents().isEmpty(), is(false));

        Intents.release();
    }

}
