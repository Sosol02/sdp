package com.github.onedirection.navigation;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.testhelpers.WaitAction;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Semaphore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

@RunWith(AndroidJUnit4.class)
public class AccountFragmentTest {

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

    private final Context ctx = ApplicationProvider.getApplicationContext();

    private final String USERNAME_1 = "UsernameTest";

    @Test
    public void testAccountExpandable() {
        onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_account)).perform(click());

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_account)));
    }

    @Test
    public void testIfWeCanChangeName() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        final boolean[] needLogout = {false};
        testRule.getScenario().onActivity(activity -> {
            if (activity.findViewById(R.id.nav_logout) != null &&
                    activity.findViewById(R.id.nav_logout).getVisibility() == View.VISIBLE) {
                needLogout[0] = true;
            }
            semaphore.release();
        });
        semaphore.acquire();

        if (needLogout[0]) {
            onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
            onView(withId(R.id.nav_logout)).perform(click());
            onView(withText(R.string.dialog_logout_yes)).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        }

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_sign)).perform(click());

        onView(withId(R.id.email)).perform(ViewActions.clearText(), ViewActions.typeText(ctx.getString(R.string.test_account)));
        onView(withId(R.id.password)).perform(ViewActions.typeText(ctx.getString(R.string.test_password)));
        onView(withId(R.id.sign)).perform(click());

        onView(withId(R.id.drawer_layout)).perform(new WaitAction(1000));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_account)).perform(click());

        onView(withId(R.id.change_username_account_display)).perform(ViewActions.clearText(), ViewActions.typeText(USERNAME_1));
        onView(withId(R.id.button_change_username)).perform(click());

        onView(withId(R.id.drawer_layout)).perform(new WaitAction(1000));

        onView(withId(R.id.nav_header_username)).check(matches(withText(USERNAME_1)));
        onView(withId(R.id.account_username)).check(matches(withText(USERNAME_1)));

        onView(withId(R.id.change_username_account_display)).perform(ViewActions.clearText(), ViewActions.typeText(ctx.getString(R.string.test_account)));
        onView(withId(R.id.button_change_username)).perform(click());

        onView(withId(R.id.drawer_layout)).perform(new WaitAction(1000));

        onView(withId(R.id.nav_header_username)).check(matches(withText(ctx.getString(R.string.test_account))));
        onView(withId(R.id.account_username)).check(matches(withText(ctx.getString(R.string.test_account))));
    }
}
