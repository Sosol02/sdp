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

public class GoogleCalendarTest {

    @Test
    public void exportButtonCanClicked() {
        FragmentScenario<ExportFragment> fragment = FragmentScenario.launchInContainer(ExportFragment.class);
        fragment.onFragment(exportFragment -> exportFragment.setExport(false));
        onView(withId(R.id.buttonGCalendarExport)).check(matches(isClickable()));
        Intents.init();

        onView(withId(R.id.buttonGCalendarExport)).perform(click());

        // This matcher is ridiculous, but I didn't find better
        Intents.intended(new TypeSafeMatcher<Intent>() {
            @Override
            protected boolean matchesSafely(Intent item) {
                return item.toString().contains("GOOGLE_SIGN_IN")
                        && item.toString().contains("com.github.onedirection");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("starts google login");
            }
        });

        Intents.release();
    }

}
