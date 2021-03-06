package com.github.onedirection.interoperability;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.interoperability.gcalendar.ExportFragment;
import com.github.onedirection.interoperability.gcalendar.ImportFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class GoogleCalendarTest {

    @Test
    public void exportButtonCanBeClicked() {
        FragmentScenario<ExportFragment> fragment = FragmentScenario.launchInContainer(ExportFragment.class);
        onView(withId(R.id.buttonGCalendarExport)).check(matches(isClickable()));
        Intents.init();
        assertThat(Intents.getIntents().isEmpty(), is(true));

        onView(withId(R.id.buttonGCalendarExport)).perform(click());

        assertThat(Intents.getIntents().isEmpty(), is(false));

        Intents.release();
    }

    @Test
    public void importButtonCanBeClicked() {
        FragmentScenario<ImportFragment> fragment = FragmentScenario.launchInContainer(ImportFragment.class);
        onView(withId(R.id.buttonGCalendarImport)).check(matches(isClickable()));
        Intents.init();
        assertThat(Intents.getIntents().isEmpty(), is(true));

        onView(withId(R.id.buttonGCalendarImport)).perform(click());

        assertThat(Intents.getIntents().isEmpty(), is(false));

        Intents.release();
    }
}
